#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
抖音短链接转长链接，并提取视频下载URL
简化版本，专注于URL提取功能
"""

import re
import requests
from urllib.parse import urlparse, parse_qs

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

def get_video_info(video_id):
    """获取视频信息
    
    Args:
        video_id (str): 视频ID
        
    Returns:
        dict: 视频信息
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        'Referer': 'https://www.douyin.com/',
    }
    
    try:
        # 构建API请求URL
        api_url = f"https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids={video_id}"
        
        # 发送请求
        response = requests.get(api_url, headers=headers)
        
        # 检查响应
        if response.status_code == 200:
            data = response.json()
            if 'item_list' in data and len(data['item_list']) > 0:
                return data['item_list'][0]
            else:
                print(f"API响应中没有找到视频信息")
        else:
            print(f"API请求失败，状态码: {response.status_code}")
        
        return None
    except Exception as e:
        print(f"获取视频信息时出错: {e}")
        return None

def extract_download_url(video_info):
    """从视频信息中提取下载URL
    
    Args:
        video_info (dict): 视频信息
        
    Returns:
        str: 视频下载URL
    """
    try:
        # 提取无水印视频URL
        if 'video' in video_info and 'play_addr' in video_info['video']:
            video_urls = video_info['video']['play_addr'].get('url_list', [])
            if video_urls:
                # 尝试获取无水印URL
                for url in video_urls:
                    if 'playwm' in url:  # 有水印
                        # 将playwm替换为play以获取无水印版本
                        no_watermark_url = url.replace('playwm', 'play')
                        return no_watermark_url
                    else:
                        return url
        
        return None
    except Exception as e:
        print(f"提取下载URL时出错: {e}")
        return None

def get_real_download_url(url):
    """获取真实的下载URL（处理重定向）
    
    Args:
        url (str): 可能需要重定向的URL
        
    Returns:
        str: 真实的下载URL
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
    }
    
    try:
        response = requests.head(url, headers=headers, allow_redirects=True)
        return response.url
    except Exception as e:
        print(f"获取真实下载URL时出错: {e}")
        return url

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

        return {
            'long_url': long_url
        }
        
        # # 3. 获取视频信息
        # video_info = get_video_info(video_id)
        # if not video_info:
        #     return {'error': '无法获取视频信息'}
        
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
    url = input("请输入抖音短链接或视频链接: ").strip()
    
    # 获取下载链接
    result = get_douyin_download_url(url)
    
    # 打印结果
    if 'error' in result:
        print(f"错误: {result['error']}")
    else:
        print("\n视频信息:")
        print(f"下载链接: {result['long_url']}")
        print("\n您可以使用浏览器或下载工具访问上述链接下载视频") 