from importlib import import_module
import os
import json
import time
import datetime
from flask import Flask, render_template, Response, request

# import camera driver
if os.environ.get('CAMERA'):
    Camera = import_module('camera_' + os.environ['CAMERA']).Camera
else:
    from camera import Camera

app = Flask(__name__)


@app.route('/')
def index():
    """Video streaming home page."""
    return render_template('index.html')

@app.route("/image", methods=['post','get'])
def get_image():
    path = request.args.get('path')
    path = "./capture_image/%s" % path
    resp = Response(open(path,'rb'),mimetype="image/jpeg")
    return resp


def gen(camera):
    """Video streaming generator function."""
    while True:
        frame = camera.get_frame()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')


@app.route('/video_start')
def video_feed():
    """Video streaming route. Put this in the src attribute of an img tag."""
    return Response(gen(Camera()),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


def TimeStampToTime(timestamp):
    raw = time.localtime(timestamp)
    return time.strftime('%Y-%m-%d %H:%M:%S',raw)

def get_FileCreateTime(filePath):
    filePath = str(filePath)
    t  = os.path.getctime(filePath)
    return TimeStampToTime(t)

@app.route('/get_all_filename')
def getfilename():
    file_dir = r"./capture_image"
    resList = []
    for file_name in os.listdir(file_dir):
        subfile = {}
        subfile['filename'] = file_name
        subfilePath = "./capture_image/" + file_name
        subfile['filedate'] = get_FileCreateTime(subfilePath)
        resList.append(subfile)
    return json.dumps(resList,ensure_ascii=False)


if __name__ == '__main__':
    app.run(host='0.0.0.0', threaded=True, port=5001)
