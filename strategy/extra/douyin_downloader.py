#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import json
import requests
from urllib.parse import urlparse, parse_qs

class DouyinDownloader:
    """抖音短链接转长链接，并提取无水印视频下载链接"""
    
    def __init__(self):
        """初始化，设置请求头"""
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'Referer': 'https://www.douyin.com/',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        }
        self.session = requests.Session()
        self.session.headers.update(self.headers)
    
    def convert_short_url(self, short_url):
        """将短链接转换为长链接
        
        Args:
            short_url (str): 抖音短链接，如 https://v.douyin.com/abcdef/
            
        Returns:
            str: 抖音长链接
        """
        try:
            # 确保URL格式正确
            if not short_url.startswith('http'):
                short_url = 'https://' + short_url
            
            # 发送请求获取重定向URL
            response = self.session.head(short_url, allow_redirects=False)
            
            # 检查是否有重定向
            if 'Location' in response.headers:
                long_url = response.headers['Location']
                print(f"短链接已转换为长链接: {long_url}")
                return long_url
            else:
                # 如果没有重定向，尝试发送GET请求
                response = self.session.get(short_url, allow_redirects=True)
                print(f"使用GET请求获取的URL: {response.url}")
                return response.url
        except Exception as e:
            print(f"转换短链接时出错: {e}")
            return short_url
    
    def extract_video_id(self, url):
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
    
    def get_video_info(self, video_id):
        """获取视频信息
        
        Args:
            video_id (str): 视频ID
            
        Returns:
            dict: 视频信息
        """
        try:
            # 构建API请求URL
            api_url = f"https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids={video_id}"
            
            # 发送请求
            response = self.session.get(api_url)
            
            # 检查响应
            if response.status_code == 200:
                data = response.json()
                if 'item_list' in data and len(data['item_list']) > 0:
                    return data['item_list'][0]
                else:
                    print(f"API响应中没有找到视频信息: {data}")
            else:
                print(f"API请求失败，状态码: {response.status_code}")
            
            return None
        except Exception as e:
            print(f"获取视频信息时出错: {e}")
            return None
    
    def extract_download_url(self, video_info):
        """从视频信息中提取下载URL
        
        Args:
            video_info (dict): 视频信息
            
        Returns:
            dict: 包含视频信息和下载链接的字典
        """
        try:
            result = {
                'title': '',
                'author': '',
                'download_url': '',
                'cover_url': '',
                'music_url': '',
            }
            
            # 提取视频标题
            if 'desc' in video_info:
                result['title'] = video_info['desc']
            
            # 提取作者信息
            if 'author' in video_info:
                result['author'] = video_info['author'].get('nickname', '')
            
            # 提取封面URL
            if 'video' in video_info and 'cover' in video_info['video']:
                cover_urls = video_info['video']['cover'].get('url_list', [])
                if cover_urls:
                    result['cover_url'] = cover_urls[0]
            
            # 提取音乐URL
            if 'music' in video_info and 'play_url' in video_info['music']:
                music_urls = video_info['music']['play_url'].get('url_list', [])
                if music_urls:
                    result['music_url'] = music_urls[0]
            
            # 提取无水印视频URL
            if 'video' in video_info and 'play_addr' in video_info['video']:
                video_urls = video_info['video']['play_addr'].get('url_list', [])
                if video_urls:
                    # 尝试获取无水印URL
                    for url in video_urls:
                        if 'playwm' in url:  # 有水印
                            # 将playwm替换为play以获取无水印版本
                            no_watermark_url = url.replace('playwm', 'play')
                            result['download_url'] = no_watermark_url
                            break
                        else:
                            result['download_url'] = url
                            break
            
            return result
        except Exception as e:
            print(f"提取下载URL时出错: {e}")
            return {'download_url': '', 'title': '', 'author': '', 'cover_url': '', 'music_url': ''}
    
    def get_real_download_url(self, url):
        """获取真实的下载URL（处理重定向）
        
        Args:
            url (str): 可能需要重定向的URL
            
        Returns:
            str: 真实的下载URL
        """
        try:
            response = self.session.head(url, allow_redirects=True)
            return response.url
        except Exception as e:
            print(f"获取真实下载URL时出错: {e}")
            return url
    
    def process_url(self, url):
        """处理URL，获取下载链接
        
        Args:
            url (str): 抖音短链接或长链接
            
        Returns:
            dict: 包含视频信息和下载链接的字典
        """
        try:
            # 1. 转换短链接为长链接
            long_url = self.convert_short_url(url)
            
            # 2. 提取视频ID
            video_id = self.extract_video_id(long_url)
            if not video_id:
                return {'error': '无法提取视频ID'}
            
            print(f"提取的视频ID: {video_id}")
            
            # 3. 获取视频信息
            video_info = self.get_video_info(video_id)
            if not video_info:
                return {'error': '无法获取视频信息'}
            
            # 4. 提取下载URL
            result = self.extract_download_url(video_info)
            
            # 5. 获取真实下载URL（处理重定向）
            if result['download_url']:
                result['download_url'] = self.get_real_download_url(result['download_url'])
                print(f"获取到的下载链接: {result['download_url']}")
            else:
                result['error'] = '无法提取下载链接'
            
            return result
        except Exception as e:
            print(f"处理URL时出错: {e}")
            return {'error': f'处理URL时出错: {str(e)}'}
    
    def download_video(self, url, output_path=None):
        """下载视频
        
        Args:
            url (str): 抖音短链接或长链接
            output_path (str, optional): 输出路径，默认为当前目录下的视频标题
            
        Returns:
            str: 下载的文件路径
        """
        try:
            # 获取下载信息
            info = self.process_url(url)
            
            if 'error' in info and info['error']:
                print(f"错误: {info['error']}")
                return None
            
            if not info['download_url']:
                print("未找到有效的下载链接")
                return None
            
            # 设置输出文件名
            title = info['title'] or f"douyin_{info['author']}"
            # 移除文件名中的非法字符
            title = re.sub(r'[\\/:*?"<>|]', '_', title)
            
            if not output_path:
                output_path = f"{title}.mp4"
            
            # 下载视频
            print(f"开始下载视频: {title}")
            response = self.session.get(info['download_url'], stream=True)
            
            total_size = int(response.headers.get('content-length', 0))
            block_size = 1024  # 1 KB
            
            with open(output_path, 'wb') as file:
                for data in response.iter_content(block_size):
                    file.write(data)
            
            print(f"视频已下载到: {output_path}")
            return output_path
        except Exception as e:
            print(f"下载视频时出错: {e}")
            return None


def main():
    """主函数"""
    # 创建下载器实例
    downloader = DouyinDownloader()
    
    # 获取用户输入的URL
    url = input("请输入抖音短链接或视频链接: ").strip()
    
    # 处理URL
    result = downloader.process_url(url)
    
    # 打印结果
    if 'error' in result and result['error']:
        print(f"错误: {result['error']}")
    else:
        print("\n视频信息:")
        print(f"标题: {result['title']}")
        print(f"作者: {result['author']}")
        print(f"下载链接: {result['download_url']}")
        
        # 询问是否下载
        download = input("\n是否下载视频? (y/n): ").strip().lower()
        if download == 'y':
            output_path = input("请输入保存路径(留空使用默认路径): ").strip()
            downloader.download_video(url, output_path if output_path else None)


if __name__ == "__main__":
    main() 