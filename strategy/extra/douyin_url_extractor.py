#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
抖音短链接转长链接，并提取视频下载URL
简化版本，专注于URL提取功能
"""

import re
import requests
from urllib.parse import urlparse, parse_qs
from bs4 import BeautifulSoup
import asyncio
from pyppeteer import launch

def convert_short_url(short_url):
    """将抖音短链接转换为长链接
    
    Args:
        short_url (str): 抖音短链接，如 https://v.douyin.com/abcdef/
        
    Returns:
        str: 抖音长链接
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
    }
    
    try:
        # 确保URL格式正确
        if not short_url.startswith('http'):
            short_url = 'https://' + short_url
        
        # 发送请求获取重定向URL
        response = requests.head(short_url, headers=headers, allow_redirects=False)
        
        # 检查是否有重定向
        if 'Location' in response.headers:
            long_url = response.headers['Location']
            print(f"短链接已转换为长链接: {long_url}")
            return long_url
        else:
            # 如果没有重定向，尝试发送GET请求
            response = requests.get(short_url, headers=headers, allow_redirects=True)
            print(f"使用GET请求获取的URL: {response.url}")
            return response.url
    except Exception as e:
        print(f"转换短链接时出错: {e}")
        return short_url

def extract_video_id(url):
    """从URL中提取视频ID
    
    Args:
        url (str): 抖音长链接
        
    Returns:
        str: 视频ID
    """
    try:
        # 尝试从URL中提取视频ID
        # 方法1: 从路径中提取
        path_pattern = r'/video/(\d+)'
        path_match = re.search(path_pattern, url)
        if path_match:
            return path_match.group(1)
        
        # 方法2: 从查询参数中提取
        parsed_url = urlparse(url)
        query_params = parse_qs(parsed_url.query)
        
        # 检查常见的视频ID参数
        for param in ['item_id', 'video_id', 'id']:
            if param in query_params:
                return query_params[param][0]
        
        # 方法3: 尝试从URL中提取任何数字序列
        id_pattern = r'/(\d{15,20})'
        id_match = re.search(id_pattern, url)
        if id_match:
            return id_match.group(1)
        
        print(f"无法从URL中提取视频ID: {url}")
        return None
    except Exception as e:
        print(f"提取视频ID时出错: {e}")
        return None

async def get_video_info(video_id):
    """获取视频信息
    
    Args:
        video_id (str): 长连接地址
        
    Returns:
        dict: 视频信息
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        'Referer': 'https://www.douyin.com/',
    }

    url = 'https://www.douyin.com/video/' + video_id  # 替换为实际抖音视频的URL
    
    browser = await launch()

    page = await browser.newPage()

    await page.goto(url)

    await asyncio.sleep(5)  # 等待页面加载完成

    video_url = await page.evaluate('''() => {'
         return document.querySelector('video').src;'
    }''')

    await browser.close()

    return video_url



def get_douyin_download_url(url):
    """从抖音链接获取视频下载URL
    
    Args:
        url (str): 抖音短链接或长链接
        
    Returns:
        dict: 包含视频信息和下载链接的字典
    """
    try:
        # 1. 转换短链接为长链接
        long_url = convert_short_url(url)
        
        # 2. 提取视频ID
        video_id = extract_video_id(long_url)
        if not video_id:
            return {'error': '无法提取视频ID'}
        print(f"提取的视频ID: {video_id}")

        # 3. 获取视频信息
        video_url = asyncio.get_event_loop().run_until_complete(get_video_info(video_id))
        if not video_url:
            return {'error': '无法获取视频地址'}
        print(f"获取到的视频地址: {video_url}")
        
        # # 4. 提取下载URL
        # download_url = extract_download_url(video_info)
        
        # # 5. 获取真实下载URL（处理重定向）
        # if download_url:
        #     real_download_url = get_real_download_url(download_url)
        #     print(f"获取到的下载链接: {real_download_url}")
            
        #     # 提取视频标题和作者
        #     title = video_info.get('desc', '')
        #     author = video_info.get('author', {}).get('nickname', '')
            
        #     return {
        #         'title': title,
        #         'author': author,
        #         'download_url': real_download_url
        #     }
        # else:
        #     return {'error': '无法提取下载链接'}
    except Exception as e:
        print(f"处理URL时出错: {e}")
        return {'error': f'处理URL时出错: {str(e)}'}

if __name__ == "__main__":
    # 获取用户输入的URL
    short_url = input("请输入抖音短链接或视频链接: ").strip()
    
    # 获取下载链接
    result = get_douyin_download_url(short_url)
    
    # 打印结果
    if 'error' in result:
        print(f"错误: {result['error']}")
    else:
        print("\n视频信息:")
        print(f"下载链接: {result['long_url']}")
        print("\n您可以使用浏览器或下载工具访问上述链接下载视频") 