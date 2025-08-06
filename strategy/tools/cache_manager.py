import sqlite3
import os
from datetime import datetime
from typing import Optional, Dict, Any, List

class SQLiteCacheManager:
    """SQLite缓存管理器，专注于股票基本信息存储"""
    
    def __init__(self, db_path: str = "stock_cache.db"):
        """
        初始化缓存管理器
        
        Args:
            db_path: 数据库文件路径
        """
        self.db_path = db_path
        self.init_database()
    
    def init_database(self):
        """初始化数据库表结构"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # 股票基本信息表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS stock_info (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                stock_code TEXT NOT NULL UNIQUE,
                stock_name TEXT,
                industry TEXT,          -- 所属行业（多个用逗号分隔）
                concept TEXT,           -- 概念板块（多个用逗号分隔）
                market TEXT,            -- 市场（主板、创业板、科创板等）
                list_date TEXT,         -- 上市日期
                total_share REAL,       -- 总股本（万股）
                circulating_share REAL, -- 流通股本（万股）
                area TEXT,              -- 地区
                pe_ratio REAL,          -- 市盈率
                pb_ratio REAL,          -- 市净率
                market_cap REAL,        -- 总市值
                circulating_cap REAL,   -- 流通市值
                open_price REAL,        -- 开盘价
                close_price REAL,       -- 收盘价
                high_price REAL,        -- 最高价
                low_price REAL,         -- 最低价
                volume REAL,            -- 成交量
                turnover_rate REAL,     -- 换手率
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # 创建索引提高查询性能
        cursor.execute('CREATE INDEX IF NOT EXISTS idx_stock_info_code ON stock_info(stock_code)')
        cursor.execute('CREATE INDEX IF NOT EXISTS idx_stock_info_industry ON stock_info(industry)')
        cursor.execute('CREATE INDEX IF NOT EXISTS idx_stock_info_market ON stock_info(market)')
        
        conn.commit()
        conn.close()
    
    def cache_stock_info(self, stock_data: Dict[str, Any]):
        """
        缓存股票基本信息
        
        Args:
            stock_data: 包含股票基本信息的字典，支持akshare的多种API返回格式
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # 处理不同API返回的字段名映射
        stock_code = self._extract_field(stock_data, ['代码', 'symbol', 'stock_code', 'code'])
        stock_name = self._extract_field(stock_data, ['名称', 'name', 'stock_name'])
        industry = self._extract_field(stock_data, ['行业', 'industry', '所属行业'])
        concept = self._extract_field(stock_data, ['概念', 'concept', '概念板块'])
        market = self._extract_field(stock_data, ['市场', 'market', '板块'])
        list_date = self._extract_field(stock_data, ['上市日期', 'list_date', 'listing_date'])
        area = self._extract_field(stock_data, ['地区', 'area', '省份'])
        
        # 数值字段处理
        total_share = self._safe_float(self._extract_field(stock_data, ['总股本', 'total_share', '总股本(万股)']))
        circulating_share = self._safe_float(self._extract_field(stock_data, ['流通股本', 'circulating_share', '流通股本(万股)']))
        pe_ratio = self._safe_float(self._extract_field(stock_data, ['市盈率', 'pe_ratio', 'PE']))
        pb_ratio = self._safe_float(self._extract_field(stock_data, ['市净率', 'pb_ratio', 'PB']))
        market_cap = self._safe_float(self._extract_field(stock_data, ['总市值', 'market_cap', '市值']))
        circulating_cap = self._safe_float(self._extract_field(stock_data, ['流通市值', 'circulating_cap']))
        
        # 价格和交易数据
        open_price = self._safe_float(self._extract_field(stock_data, ['开盘', 'open', '开盘价']))
        close_price = self._safe_float(self._extract_field(stock_data, ['收盘', 'close', '收盘价', '最新价']))
        high_price = self._safe_float(self._extract_field(stock_data, ['最高', 'high', '最高价']))
        low_price = self._safe_float(self._extract_field(stock_data, ['最低', 'low', '最低价']))
        volume = self._safe_float(self._extract_field(stock_data, ['成交量', 'volume', '成交手']))
        turnover_rate = self._safe_float(self._extract_field(stock_data, ['换手率', 'turnover_rate', '换手']))
        
        cursor.execute('''
            INSERT OR REPLACE INTO stock_info 
            (stock_code, stock_name, industry, concept, market, list_date, 
             total_share, circulating_share, area, pe_ratio, pb_ratio, 
             market_cap, circulating_cap, open_price, close_price, high_price, 
             low_price, volume, turnover_rate, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            stock_code, stock_name, industry, concept, market, list_date,
            total_share, circulating_share, area, pe_ratio, pb_ratio,
            market_cap, circulating_cap, open_price, close_price, high_price,
            low_price, volume, turnover_rate, datetime.now()
        ))
        
        conn.commit()
        conn.close()
        
        print(f"已缓存股票信息: {stock_code} - {stock_name}")
    
    def batch_cache_stock_info(self, stock_list: List[Dict[str, Any]]):
        """
        批量缓存股票基本信息
        
        Args:
            stock_list: 股票信息列表
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cached_count = 0
        for stock_data in stock_list:
            try:
                # 处理字段映射
                stock_code = self._extract_field(stock_data, ['代码', 'symbol', 'stock_code', 'code'])
                if not stock_code:
                    continue
                    
                stock_name = self._extract_field(stock_data, ['名称', 'name', 'stock_name'])
                industry = self._extract_field(stock_data, ['行业', 'industry', '所属行业'])
                concept = self._extract_field(stock_data, ['概念', 'concept', '概念板块'])
                market = self._extract_field(stock_data, ['市场', 'market', '板块'])
                list_date = self._extract_field(stock_data, ['上市日期', 'list_date', 'listing_date'])
                area = self._extract_field(stock_data, ['地区', 'area', '省份'])
                
                # 数值字段处理
                total_share = self._safe_float(self._extract_field(stock_data, ['总股本', 'total_share', '总股本(万股)']))
                circulating_share = self._safe_float(self._extract_field(stock_data, ['流通股本', 'circulating_share', '流通股本(万股)']))
                pe_ratio = self._safe_float(self._extract_field(stock_data, ['市盈率', 'pe_ratio', 'PE']))
                pb_ratio = self._safe_float(self._extract_field(stock_data, ['市净率', 'pb_ratio', 'PB']))
                market_cap = self._safe_float(self._extract_field(stock_data, ['总市值', 'market_cap', '市值']))
                circulating_cap = self._safe_float(self._extract_field(stock_data, ['流通市值', 'circulating_cap']))
                
                # 价格和交易数据
                open_price = self._safe_float(self._extract_field(stock_data, ['开盘', 'open', '开盘价']))
                close_price = self._safe_float(self._extract_field(stock_data, ['收盘', 'close', '收盘价', '最新价']))
                high_price = self._safe_float(self._extract_field(stock_data, ['最高', 'high', '最高价']))
                low_price = self._safe_float(self._extract_field(stock_data, ['最低', 'low', '最低价']))
                volume = self._safe_float(self._extract_field(stock_data, ['成交量', 'volume', '成交手']))
                turnover_rate = self._safe_float(self._extract_field(stock_data, ['换手率', 'turnover_rate', '换手']))
                
                cursor.execute('''
                    INSERT OR REPLACE INTO stock_info 
                    (stock_code, stock_name, industry, concept, market, list_date, 
                     total_share, circulating_share, area, pe_ratio, pb_ratio, 
                     market_cap, circulating_cap, open_price, close_price, high_price, 
                     low_price, volume, turnover_rate, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', (
                    stock_code, stock_name, industry, concept, market, list_date,
                    total_share, circulating_share, area, pe_ratio, pb_ratio,
                    market_cap, circulating_cap, open_price, close_price, high_price,
                    low_price, volume, turnover_rate, datetime.now()
                ))
                
                cached_count += 1
                
            except Exception as e:
                print(f"缓存股票信息失败: {stock_data}, 错误: {e}")
                continue
        
        conn.commit()
        conn.close()
        
        print(f"批量缓存完成，成功缓存 {cached_count} 只股票信息")
    
    def get_stock_info(self, stock_code: str) -> Optional[Dict[str, Any]]:
        """
        获取股票基本信息
        
        Args:
            stock_code: 股票代码
        
        Returns:
            Dict: 股票基本信息，如果不存在返回None
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute('SELECT * FROM stock_info WHERE stock_code = ?', (stock_code,))
        result = cursor.fetchone()
        
        if result:
            columns = [description[0] for description in cursor.description]
            stock_info = dict(zip(columns, result))
            conn.close()
            return stock_info
        
        conn.close()
        return None
    
    def get_stocks_by_industry(self, industry_name: str) -> List[Dict[str, Any]]:
        """
        根据行业获取股票列表
        
        Args:
            industry_name: 行业名称（支持模糊匹配）
        
        Returns:
            List[Dict]: 股票信息列表
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute('''
            SELECT * FROM stock_info 
            WHERE industry LIKE ? OR concept LIKE ?
            ORDER BY market_cap DESC
        ''', (f'%{industry_name}%', f'%{industry_name}%'))
        
        results = cursor.fetchall()
        columns = [description[0] for description in cursor.description]
        
        stocks = [dict(zip(columns, row)) for row in results]
        conn.close()
        
        return stocks
    
    def get_cache_stats(self) -> Dict[str, int]:
        """
        获取缓存统计信息
        
        Returns:
            Dict: 缓存统计信息
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute('SELECT COUNT(*) FROM stock_info')
        stock_count = cursor.fetchone()[0]
        
        conn.close()
        
        return {
            'total_stocks': stock_count,
            'db_size_mb': round(os.path.getsize(self.db_path) / 1024 / 1024, 2) if os.path.exists(self.db_path) else 0
        }
    
    def clear_all_cache(self):
        """
        清空所有缓存数据
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        cursor.execute('DELETE FROM stock_info')
        
        conn.commit()
        conn.close()
        
        print("已清空所有缓存数据")
    
    def _extract_field(self, data: Dict[str, Any], field_names: List[str]) -> Optional[str]:
        """
        从数据字典中提取字段值（支持多个可能的字段名）
        
        Args:
            data: 数据字典
            field_names: 可能的字段名列表
        
        Returns:
            str: 字段值，如果都不存在返回None
        """
        for field_name in field_names:
            if field_name in data and data[field_name] is not None:
                value = str(data[field_name]).strip()
                return value if value and value != 'nan' and value != '-' else None
        return None
    
    def _safe_float(self, value: Any) -> Optional[float]:
        """
        安全转换为浮点数
        
        Args:
            value: 待转换的值
        
        Returns:
            float: 转换后的浮点数，失败返回None
        """
        if value is None:
            return None
        try:
            if isinstance(value, str):
                # 处理包含单位的数值（如"123.45万"）
                value = value.replace(',', '').replace('万', '').replace('亿', '0000')
                if value in ['', '-', 'nan', 'None']:
                    return None
            return float(value)
        except (ValueError, TypeError):
            return None