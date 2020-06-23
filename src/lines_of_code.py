import glob

def file_len(filename):
    return sum(1 for line in open(filename))

files = glob.glob("." + '/**/*.kt', recursive=True)
total = sum(map(file_len, files))

print(total)