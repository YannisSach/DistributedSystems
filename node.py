import hashlib

low = 0
high = 9
chord_size = 10

bucket = {}

def hashf (key):
    sha = hashlib.sha1()
    key = key.encode('utf-8')
    sha.update(key)
    hashed_key = sha.hexdigest()
    hashed_key = int(hashed_key, 16)
    return hashed_key % chord_size

def is_mine (key):

    if low < high:
        return low <= key <= high
    else:
        return low <= key or key <= high


def insert (key, value):

    # key: title of the song
    # value: the song
    hashed_key = hashf(key)

    if is_mine(hashed_key):
        ls = bucket[hashed_key]
        for x in ls:
            if x[0] = key:
                ls.remove(x)

        bucket[hashed_key].append((key,value))
    else:
        # forward to the next node

def query (key):

    # check if key is *

    hashed_key = hashf(key)

    if is_mine(hashed_key):
        ls = bucket[hashed_key]
        for x in ls:
            if x[0] = key:
                return x

        return (0,0)

    else:
        # forward

def delete (key):

    hashed_key = hashf(key)

    if is_mine(hashed_key):
        ls = bucket[hashed_key]
        for x in ls:
            if x[0] = key:
                ls.remove(x)

        return (0,0)

    else:
        # forward
