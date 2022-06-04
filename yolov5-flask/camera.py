import os
import cv2
from base_camera import BaseCamera
import torch
import torch.nn as nn
import torchvision
import numpy as np
import argparse
from utils.datasets import *
from utils.utils import *



class Camera(BaseCamera):
    video_source = 'ship.mp4'

    def __init__(self):
        if os.environ.get('OPENCV_CAMERA_SOURCE'):
            Camera.set_video_source(int(os.environ['OPENCV_CAMERA_SOURCE']))
        super(Camera, self).__init__()

    @staticmethod
    def set_video_source(source):
        Camera.video_source = source

    @staticmethod
    def frames():
        #define Conifig
        counter = 0
        frequency = 10
        cateDict = {'ore carrier':[0,0],'general cargo ship':[0,0],'bulk cargo carrier':[0,0],'container ship':[0,0],'fishing boat':[0,0],'passenger ship':[0,0],'doll':[0,0]}
        out, weights, imgsz = \
        'inference/output', 'runs/exp15/weights/best.pt', 640
        source = 'ship.mp4'
        # source = 0
        device = torch_utils.select_device()
        if os.path.exists(out):
            shutil.rmtree(out)  # delete output folder
        os.makedirs(out)  # make new output folder

        # Load model
        google_utils.attempt_download(weights)
        model = torch.load(weights, map_location=device)['model']
        
        model.to(device).eval()

        # if you use cpu, pls use this
        # model.to(device).float().eval()

        # Second-stage classifier
        classify = False
        if classify:
            modelc = torch_utils.load_classifier(name='resnet101', n=2)  # initialize
            modelc.load_state_dict(torch.load('weights/resnet101.pt', map_location=device)['model'])  # load weights
            modelc.to(device).eval()

        # Half precision
        # half = False and device.type != 'cpu'
        half = True and device.type != 'cpu'
        print('half = ' + str(half))

        if half:
            model.half()

        # Set Dataloader
        vid_path, vid_writer = None, None
        dataset = LoadImages(source, img_size=imgsz)
        #dataset = LoadStreams(source, img_size=imgsz)
        names = model.names if hasattr(model, 'names') else model.modules.names
        colors = [[random.randint(0, 255) for _ in range(3)] for _ in range(len(names))]

        # Run inference
        t0 = time.time()
        img = torch.zeros((1, 3, imgsz, imgsz), device=device)  # init img
        _ = model(img.half() if half else img) if device.type != 'cpu' else None  # run once
        #frameCounter = 0
        for path, img, im0s, vid_cap in dataset:
            #frameCounter += 1
            #if frameCounter % 10 != 0:
              #  continue
            #print("frameCounter %d",frameCounter)
            img = torch.from_numpy(img).to(device)
            img = img.half() if half else img.float()  # uint8 to fp16/32
            img /= 255.0  # 0 - 255 to 0.0 - 1.0
            if img.ndimension() == 3:
                img = img.unsqueeze(0)

            # Inference
            t1 = torch_utils.time_synchronized()
            pred = model(img, augment=False)[0]
            
            # Apply NMS
            pred = non_max_suppression(pred, 0.4, 0.5,
                               fast=True, classes=None, agnostic=False)
            t2 = torch_utils.time_synchronized()

            # Apply Classifier
            if classify:
                pred = apply_classifier(pred, modelc, img, im0s)

            for i, det in enumerate(pred):  # detections per image
                p, s, im0 = path, '', im0s

                save_path = str(Path(out) / Path(p).name)
                s += '%gx%g ' % img.shape[2:]  # print string
                gn = torch.tensor(im0.shape)[[1, 0, 1, 0]]  #  normalization gain whwh
                counter = counter + 1
                if det is not None and len(det):
                    # Rescale boxes from img_size to im0 size
                    det[:, :4] = scale_coords(img.shape[2:], det[:, :4], im0.shape).round()
                    #for c in det[:, -1].unique():  #probably error with torch 1.5
                    for c in det[:, -1].detach().unique():
                        n = (det[:, -1] == c).sum()  # detections per class
                        s += '%g %s, ' % (n, names[int(c)])  # add to string
                        cateDict[names[int(c)]][1] = n
                    cateFlag = False
                    for key in cateDict:
                        if cateDict[key][0]!= cateDict[key][1]:
                            cateDict[key][0] = cateDict[key][1]
                            cateFlag = True
                    #Draw boxes    
                    for *xyxy, conf, cls in det:
                        label = '%s %.2f' % (names[int(cls)], conf)
                        plot_one_box(xyxy, im0, label=label, color=colors[int(cls)], line_thickness=3)
                if(counter % frequency == 0) & cateFlag:
                    cv2.imwrite("./capture_image/"+str(counter)+'.jpg',im0)
                    print('%sDone. (%.3fs) Counter: %d' % (s, t2 - t1,counter))
                else:
                    s = ''
 
            yield cv2.imencode('.jpg', im0)[1].tobytes()
