#!usr/bin/python
# coding = utf-8
import os
import shutil
import sys
import zipfile


def getChannels():
    f = open("./channels", "r+", -1, "utf-8")
    channel_list = []
    for line in f:
        channel = line.split(" ")[-1]
        channel = channel.strip('"')
        channel = channel.rstrip('\n"')
        channel_list.append(channel)
    return channel_list


def mkdirs(path):
    path = path.strip()
    path = path.strip("\\")

    isExist = os.path.exists(path)
    if not isExist:
        os.makedirs(path)


##test release
def get_source_path():
    if len(sys.argv) > 1:
        return sys.argv[1]
    else:
        return ""


print("copy action begin")
src = get_source_path()
channels = getChannels()
# src = "./CmtMedia.apk"
# channels = ['1', '2', '3']
for i in range(len(channels)):
    print("channel == " + channels[i])
    newName = "./CmtMedia" + "_" + channels[i] + ".apk"
    shutil.copy(src, newName)
    zipf = zipfile.ZipFile(newName, mode="a")
    print(zipf.filename + "....")
    writeData = bytes(channels[i], encoding="utf-8")
    zipf.comment = writeData
    print(zipf.comment)
print("copy action finished")
