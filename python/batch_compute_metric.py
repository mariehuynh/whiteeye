#!/usr/bin/python

import colorsys
import os
import sys

from PIL import Image

def compute_metric(image):
    rs, gs, bs = image.convert("RGB").split()
    metric = 0
    rmean = sum(rs.getdata()) / len(rs.getdata())
    gmean = sum(gs.getdata()) / len(gs.getdata())
    bmean = sum(bs.getdata()) / len(bs.getdata())

    h, s, v = colorsys.rgb_to_hsv(rmean / 255.0, gmean / 255.0, bmean / 255.0)
    if h > 0.8:
        h = h - 1  # wrap around angular coordinate to be near 0

    metric = max(0.1, 1 - (h - 0.2)**2) / (2 * s * s + (1 - v) * (1 - v) + 0.01)
    return metric

files = sys.argv[1:]
if not files:
    sys.exit("Usage: python batch_compute_metric.py [files]")

for infile in files:
    file, ext = os.path.splitext(infile)
    im = Image.open(infile)
    # print(im.mode)

    print("{0:s}\t{1:f}".format(infile, compute_metric(im)))
