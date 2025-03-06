import redis

def connect_redis():
    # 连接到本地 Redis 服务器
    client = redis.StrictRedis(host='127.0.0.1', port=6379, db=0)
    return client

def simple_query(client, key):
    # 查询 Redis 中的键值
    value = client.get(key)
    return value

if __name__ == "__main__":
    client = connect_redis()
    key = 'example_key'
    value = simple_query(client, key)
    if value:
        print(f"Key: {key}, Value: {value.decode('utf-8')}")
    else:
        print(f"Key: {key} does not exist.")