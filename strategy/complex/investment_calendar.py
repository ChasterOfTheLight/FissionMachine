import pandas as pd
from datetime import datetime, timedelta
import requests
from bs4 import BeautifulSoup
import re
import json
import time

class InvestmentCalendar:
    def __init__(self):
        self.today = datetime.now()
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'Referer': 'https://www.cls.cn/investKalendar',
            'Content-Type': 'application/json;charset=UTF-8',
            'Origin': 'https://www.cls.cn'
        }

    def get_api_data(self):
        """通过财联社API获取投资日历数据"""
        try:
            # 使用用户提供的财联社投资日历API
            api_url = "https://www.cls.cn/api/calendar/web/list"
            
            # 构造请求参数
            params = {
                "app": "CailianpressWeb",
                "flag": "0",
                "os": "web",
                "sv": "8.4.6",
                "type": "0", 
                "sign": "4b839750dc2f6b803d1c8ca00d2b40be"
            }
            
            print("正在从财联社获取投资日历数据...")
            response = requests.get(api_url, headers=self.headers, params=params)
            
            if response.status_code == 200:
                data = response.json()
                return data
            else:
                print(f"API请求失败，状态码: {response.status_code}")
                return None
        except Exception as e:
            print(f"获取API数据失败: {str(e)}")
            return None

    def extract_calendar_from_api(self, api_data):
        """从API数据中提取日历事件"""
        events_data = []
        
        # 检查 API 响应是否有效
        if not api_data or 'data' not in api_data:
            print("API数据格式不正确或没有数据")
            return events_data
        
        today_date = self.today.date()
        
        for item in api_data['data']:
            try:
                day_items = item['items']
                for day_item in day_items:
                        # 提取标题和内容
                        title = day_item.get('title', '')
                        
                        # 添加到结果中
                        events_data.append({
                            '日历': day_item.get('calendar_time', ''),
                            '事件': title
                        })
            except Exception as e:
                print(f"解析事件失败: {str(e)}")
                continue
                
        return events_data

    def get_webpage_data(self):
        """从财联社网页直接获取投资日历数据"""
        try:
            web_url = "https://www.cls.cn/investKalendar"
            print("正在从财联社网页获取投资日历数据...")
            
            response = requests.get(web_url, headers=self.headers)
            soup = BeautifulSoup(response.content, 'html.parser')
            
            # 查找页面加载的JavaScript数据
            scripts = soup.find_all('script')
            data_script = None
            
            for script in scripts:
                if script.string and "window.__INITIAL_STATE__" in script.string:
                    data_script = script.string
                    break
            
            if data_script:
                # 从JavaScript中提取JSON数据
                json_str = re.search(r'window\.__INITIAL_STATE__\s*=\s*({.*?});', data_script, re.DOTALL)
                if json_str:
                    data = json.loads(json_str.group(1))
                    return data
            
            # 如果无法找到数据，尝试解析DOM结构
            return self.parse_dom_structure(soup)
            
        except Exception as e:
            print(f"获取网页数据失败: {str(e)}")
            return None
    
    def parse_dom_structure(self, soup):
        """解析网页DOM结构提取日历数据"""
        events_data = []
        
        try:
            # 寻找日历项容器
            calendar_items = soup.select('.cls-telegraph-item, .calendar-item, .investment-calendar-item')
            today_date = self.today.date()
            
            for item in calendar_items:
                try:
                    # 尝试提取日期和时间
                    date_elem = item.select_one('.date, .calendar-date')
                    time_elem = item.select_one('.time, .calendar-time')
                    title_elem = item.select_one('.title, .calendar-title, .event-title')
                    
                    if date_elem and title_elem:
                        date_text = date_elem.text.strip()
                        
                        # 尝试解析日期（可能有多种格式）
                        try:
                            if '-' in date_text:
                                event_date = datetime.strptime(date_text, '%Y-%m-%d').date()
                            elif '/' in date_text:
                                event_date = datetime.strptime(date_text, '%Y/%m/%d').date()
                            elif '月' in date_text and '日' in date_text:
                                # 处理中文日期格式如"6月10日"
                                current_year = self.today.year
                                month = int(date_text.split('月')[0])
                                day = int(date_text.split('月')[1].replace('日', ''))
                                event_date = datetime(current_year, month, day).date()
                            else:
                                # 跳过无法识别的日期
                                continue
                        except ValueError:
                            continue
                        
                        # 只保留今天及以后的事件
                        if event_date >= today_date:
                            time_text = time_elem.text.strip() if time_elem else "00:00"
                            title_text = title_elem.text.strip()
                            
                            events_data.append({
                                'date': event_date,
                                'time': time_text,
                                'title': title_text
                            })
                except Exception as e:
                    print(f"解析日历项失败: {str(e)}")
                    continue
        except Exception as e:
            print(f"DOM解析失败: {str(e)}")
        
        return {'events': events_data}

    def extract_calendar_from_webpage(self, web_data):
        """从网页数据中提取日历事件"""
        events_data = []
        
        # 如果已经通过DOM解析获取了事件
        if 'events' in web_data:
            return web_data['events']
        
        # 尝试从网页加载的状态数据中提取
        try:
            if 'investKalendar' in web_data and 'calendarList' in web_data['investKalendar']:
                calendar_list = web_data['investKalendar']['calendarList']
                
                for item in calendar_list:
                    try:
                        events_data.append({
                            'title': item.get('title', ''),
                            'content': item.get('content', '')
                        })
                    except Exception as e:
                        print(f"解析日历项失败: {str(e)}")
                        continue
        except Exception as e:
            print(f"从网页数据提取日历失败: {str(e)}")
        
        return events_data

    def get_economic_calendar(self):
        """获取金融经济大事件日历"""
        print("\n=== 财联社投资日历 ===")
        print(f"获取日期: {self.today.strftime('%Y-%m-%d')} 及以后")
        
        all_events = []
        
        # 使用API获取数据
        api_data = self.get_api_data()
        if api_data:
            api_events = self.extract_calendar_from_api(api_data)
            if api_events:
                print(f"通过API获取到 {len(api_events)} 条事件")
                all_events.extend(api_events)
        
        # 如果API获取不到数据或数据不足，尝试从网页获取
        if not all_events:
            web_data = self.get_webpage_data()
            if web_data:
                web_events = self.extract_calendar_from_webpage(web_data)
                if web_events:
                    print(f"通过网页获取到 {len(web_events)} 条事件")
                    all_events.extend(web_events)
        
        # 处理并显示结果
        if all_events:
            # 转换为DataFrame并排序
            df = pd.DataFrame(all_events)
            df = df.sort_values(['日历'])
            
            # 格式化输出
            print("\n投资日历事件:")
            pd.set_option('display.max_columns', None)
            pd.set_option('display.width', None)
            pd.set_option('display.max_colwidth', 80)  # 限制标题长度，避免输出过宽
            print(df.to_string(index=False))
            
            print(f"\n共计 {len(df)} 条事件")
            return df
        else:
            print("未获取到投资日历数据")
            return pd.DataFrame()

    def generate_investment_calendar(self):
        """生成投资日历"""
        print(f"\n=== 投资日历生成器 ({self.today.strftime('%Y-%m-%d')}) ===")
        self.get_economic_calendar()

def main():
    calendar = InvestmentCalendar()
    calendar.generate_investment_calendar()

if __name__ == "__main__":
    main() 