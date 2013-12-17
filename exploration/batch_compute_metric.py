#!/usr/bin/python
"""
Try to formulate a metric to separate photographs of pupils into those with
Leukocoria and those without.

This metric was motivated by the findings of:
http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0076677#s2

See particularly figure 10.

"""

import colorsys
import os
import sys

from PIL import Image

def compute_metric(image):
    # Compute average color over the sample image
    rs, gs, bs = image.convert("RGB").split()
    rmean = sum(rs.getdata()) / len(rs.getdata())
    gmean = sum(gs.getdata()) / len(gs.getdata())
    bmean = sum(bs.getdata()) / len(bs.getdata())

    # rgb_to_hsv inputs and outputs coords in the range [0, 1].
    h, s, v = colorsys.rgb_to_hsv(rmean / 256.0, gmean / 256.0, bmean / 256.0)

    # Leukocoria is clustered around hue = 0.07, with a notable outlier, so
    # make fairly broad.
    h_center = 0.07
    if h > 0.5 + h_center:
        h -= 1  # wrap angular coordinate to be symmetric about h_center
    metric_h = 1 - (h - h_center)

    # Clustered around the (0, 1) coordinate in (saturation, value). Use
    # elliptical contours and invert so that the highest value is at (0, 1).
    # Add 0.01 to prevent division by zero.
    # FIXME: I'm concerned that variations in exposure will directly translate into
    #        variations in value. We'll want to verify sufficient exposure by
    #        checking outside of the cropped area.
    metric_sv = 1 / (1.36 * s * s + (1 - v) * (1 - v) + 0.01)
    metric = metric_h * metric_sv
    return metric

files = sys.argv[1:]
if not files:
    sys.exit("Usage: python batch_compute_metric.py [files]")

for infile in files:
    file, ext = os.path.splitext(infile)
    im = Image.open(infile)

    print("{0:s}\t{1:f}".format(infile, compute_metric(im)))
