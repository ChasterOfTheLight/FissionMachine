import pandas as pd
import numpy as np
import akshare as ak
import talib as ta
from datetime import datetime, timedelta
import os

class StockSelector:
    """
    多因子选股模型
    基于价值、成长、质量、动量、波动等多维度因子进行选股
    """
    
    def __init__(self, 
                 start_date=None, 
                 end_date=None,
                 stock_pool='hs300',
                 top_n=50,
                 cache_dir='./data_cache',
                 use_fundamental=True):
        """
        初始化选股器
        
        参数:
            start_date: 开始日期，格式 'YYYY-MM-DD'
            end_date: 结束日期，格式 'YYYY-MM-DD'
            stock_pool: 股票池，可选 'hs300'(沪深300), 'zz500'(中证500), 'all_a'(全部A股)
            top_n: 选择排名前多少的股票
            cache_dir: 数据缓存目录
            use_fundamental: 是否使用基本面数据进行选股，如果为False则仅使用技术指标
        """
        self.start_date = start_date or (datetime.now() - timedelta(days=365)).strftime('%Y-%m-%d')
        self.end_date = end_date or datetime.now().strftime('%Y-%m-%d')
        self.stock_pool = stock_pool
        self.top_n = top_n
        self.cache_dir = cache_dir
        self.use_fundamental = use_fundamental
        
        # 确保缓存目录存在
        if not os.path.exists(cache_dir):
            os.makedirs(cache_dir)
            
        # 初始化股票池
        self._init_stock_pool()
        
    def _init_stock_pool(self):
        """初始化股票池"""
        print(f"初始化股票池: {self.stock_pool}")
        
        if self.stock_pool == 'hs300':
            # 获取沪深300成分股
            self.stocks = self._get_hs300_stocks()
        elif self.stock_pool == 'zz500':
            # 获取中证500成分股
            self.stocks = self._get_zz500_stocks()
        elif self.stock_pool == 'all_a':
            # 获取所有A股
            self.stocks = self._get_all_a_stocks()
        else:
            raise ValueError(f"不支持的股票池: {self.stock_pool}")
            
        print(f"股票池初始化完成，共 {len(self.stocks)} 只股票")
    
    def _get_hs300_stocks(self):
        """获取沪深300成分股"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, 'hs300_stocks.csv')
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                return df['symbol'].tolist()
            
            # 从AKShare获取沪深300成分股
            df = ak.index_stock_cons_weight_csindex(symbol="000300")
            df['symbol'] = df['成分券代码'].apply(lambda x: x + '.SH' if x.startswith('6') else x + '.SZ')
            
            # 保存到缓存
            df[['symbol']].to_csv(cache_file, index=False)
            return df['symbol'].tolist()
        except Exception as e:
            print(f"获取沪深300成分股失败: {e}")
            # 如果失败，返回一些常见的沪深300成分股作为备选
            return ['600519.SH', '601318.SH', '600036.SH', '000858.SZ', '601166.SH', 
                    '000333.SZ', '600276.SH', '601888.SH', '600030.SH', '601012.SH']
    
    def _get_zz500_stocks(self):
        """获取中证500成分股"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, 'zz500_stocks.csv')
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                return df['symbol'].tolist()
            
            # 从AKShare获取中证500成分股
            df = ak.index_stock_cons_weight_csindex(symbol="000905")
            df['symbol'] = df['成分券代码'].apply(lambda x: x + '.SH' if x.startswith('6') else x + '.SZ')
            
            # 保存到缓存
            df[['symbol']].to_csv(cache_file, index=False)
            return df['symbol'].tolist()
        except Exception as e:
            print(f"获取中证500成分股失败: {e}")
            # 返回一些常见的中证500成分股作为备选
            return ['600009.SH', '600019.SH', '600021.SH', '600022.SH', '600026.SH',
                    '000009.SZ', '000012.SZ', '000021.SZ', '000025.SZ', '000027.SZ']
    
    def _get_all_a_stocks(self):
        """获取所有A股"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, 'all_a_stocks.csv')
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                return df['symbol'].tolist()
            
            # 从AKShare获取所有A股
            stock_zh_a_spot_df = ak.stock_zh_a_spot()
            
            # 转换为标准格式
            symbols = []
            for _, row in stock_zh_a_spot_df.iterrows():
                code = row['代码']
                if code.startswith('6'):
                    symbols.append(f"{code}.SH")
                else:
                    symbols.append(f"{code}.SZ")
            
            # 保存到缓存
            pd.DataFrame({'symbol': symbols}).to_csv(cache_file, index=False)
            return symbols
        except Exception as e:
            print(f"获取所有A股失败: {e}")
            # 返回一些常见股票作为备选
            return self._get_hs300_stocks()[:100]
    
    def _get_stock_data(self, symbol, start_date, end_date):
        """获取单只股票的历史数据"""
        try:
            # 尝试从缓存加载
            cache_file = os.path.join(self.cache_dir, f"{symbol.replace('.', '_')}.csv")
            if os.path.exists(cache_file):
                df = pd.read_csv(cache_file)
                df['date'] = pd.to_datetime(df['date'])
                # 过滤日期范围
                df = df[(df['date'] >= start_date) & (df['date'] <= end_date)]
                if not df.empty:
                    return df
            
            # 从AKShare获取股票数据
            stock_code = symbol.split('.')[0]
            market = 'sh' if symbol.endswith('.SH') else 'sz'
            
            # 获取日线数据
            df = ak.stock_zh_a_hist(symbol=stock_code, period="daily", 
                                    start_date=start_date.replace('-', ''), 
                                    end_date=end_date.replace('-', ''),
                                    adjust="qfq")
            
            # 重命名列
            df = df.rename(columns={
                '日期': 'date',
                '开盘': 'open',
                '收盘': 'close',
                '最高': 'high',
                '最低': 'low',
                '成交量': 'volume',
                '成交额': 'amount',
                '振幅': 'amplitude',
                '涨跌幅': 'pct_change',
                '涨跌额': 'change',
                '换手率': 'turnover'
            })
            
            # 转换日期格式
            df['date'] = pd.to_datetime(df['date'])
            
            # 保存到缓存
            df.to_csv(cache_file, index=False)
            return df
        except Exception as e:
            print(f"获取股票 {symbol} 数据失败: {e}")
            return pd.DataFrame()
    
    def _calculate_factors(self, df):
        """计算选股因子"""
        if df.empty:
            return {}
        
        factors = {}
        
        # 1. 动量因子
        # 计算过去20日、60日收益率
        factors['momentum_20d'] = df['close'].pct_change(20).iloc[-1] if len(df) > 20 else np.nan
        factors['momentum_60d'] = df['close'].pct_change(60).iloc[-1] if len(df) > 60 else np.nan
        
        # 2. 波动率因子
        # 计算过去20日、60日收益率标准差
        factors['volatility_20d'] = df['close'].pct_change().rolling(20).std().iloc[-1] if len(df) > 20 else np.nan
        factors['volatility_60d'] = df['close'].pct_change().rolling(60).std().iloc[-1] if len(df) > 60 else np.nan
        
        # 3. 技术指标因子
        # RSI - 相对强弱指数
        if len(df) >= 14:
            factors['rsi_14'] = ta.RSI(df['close'].values, timeperiod=14)[-1]
        else:
            factors['rsi_14'] = np.nan
            
        # MACD
        if len(df) >= 26:
            macd, macd_signal, macd_hist = ta.MACD(df['close'].values, 
                                                  fastperiod=12, 
                                                  slowperiod=26, 
                                                  signalperiod=9)
            factors['macd'] = macd[-1]
            factors['macd_signal'] = macd_signal[-1]
            factors['macd_hist'] = macd_hist[-1]
        else:
            factors['macd'] = np.nan
            factors['macd_signal'] = np.nan
            factors['macd_hist'] = np.nan
            
        # 布林带
        if len(df) >= 20:
            upper, middle, lower = ta.BBANDS(df['close'].values, timeperiod=20)
            factors['bb_width'] = (upper[-1] - lower[-1]) / middle[-1]  # 布林带宽度
            factors['bb_position'] = (df['close'].iloc[-1] - lower[-1]) / (upper[-1] - lower[-1])  # 价格在布林带中的位置
        else:
            factors['bb_width'] = np.nan
            factors['bb_position'] = np.nan
            
        # 4. 成交量因子
        # 成交量变化率
        factors['volume_change_20d'] = (df['volume'].iloc[-1] / df['volume'].iloc[-20] - 1) if len(df) > 20 else np.nan
        
        # 5. 趋势因子
        # 价格相对于均线的位置
        if len(df) >= 20:
            df['ma20'] = df['close'].rolling(20).mean()
            factors['price_to_ma20'] = df['close'].iloc[-1] / df['ma20'].iloc[-1] - 1
        else:
            factors['price_to_ma20'] = np.nan
            
        if len(df) >= 60:
            df['ma60'] = df['close'].rolling(60).mean()
            factors['price_to_ma60'] = df['close'].iloc[-1] / df['ma60'].iloc[-1] - 1
        else:
            factors['price_to_ma60'] = np.nan
            
        # 6. 反转因子
        # 过去5日最低价与当前价格的距离
        if len(df) >= 5:
            factors['reversion_5d'] = df['close'].iloc[-1] / df['low'].iloc[-5:].min() - 1
        else:
            factors['reversion_5d'] = np.nan
            
        return factors
    
    def _get_fundamental_data(self, symbol):
        """获取基本面数据"""
        try:
            # 从缓存加载
            cache_file = os.path.join(self.cache_dir, f"{symbol.replace('.', '_')}_fundamental.csv")
            if os.path.exists(cache_file):
                return pd.read_csv(cache_file, index_col=0).to_dict()
            
            # 获取股票代码（不带后缀）
            stock_code = symbol.split('.')[0]
            market = "sh" if symbol.endswith(".SH") else "sz"
            
            fundamental_data = {}
            
            # 1. 获取最新的股价数据
            try:
                # 获取最近的收盘价
                stock_data = self._get_stock_data(symbol, 
                                                 (datetime.now() - timedelta(days=30)).strftime('%Y-%m-%d'), 
                                                 datetime.now().strftime('%Y-%m-%d'))
                if not stock_data.empty:
                    latest_price = stock_data['close'].iloc[-1]
                    fundamental_data['latest_price'] = latest_price
                else:
                    print(f"无法获取 {symbol} 的最新价格数据")
                    latest_price = np.nan
            except Exception as e:
                print(f"获取 {symbol} 价格数据失败: {e}")
                latest_price = np.nan
            
            # 2. 获取财务数据 - 使用更安全的方式
            # 2.1 获取资产负债表数据
            balance_sheet = None
            try:
                # 使用安全的方式获取资产负债表
                try:
                    balance_sheet = ak.stock_balance_sheet_by_report_em(symbol=stock_code)
                except Exception as e:
                    print(f"获取 {symbol} 资产负债表数据异常: {e}")
                    balance_sheet = None
                
                # 安全检查返回的数据
                if balance_sheet is None or not isinstance(balance_sheet, pd.DataFrame) or balance_sheet.empty:
                    print(f"获取 {symbol} 资产负债表数据返回无效结果")
                    balance_sheet = None
            except Exception as e:
                print(f"处理 {symbol} 资产负债表数据过程中发生错误: {e}")
                balance_sheet = None
            
            # 如果成功获取资产负债表，安全地提取数据
            if balance_sheet is not None:
                try:
                    # 确保至少有一行数据
                    if len(balance_sheet) > 0:
                        latest_balance = balance_sheet.iloc[0]
                        
                        # 安全地获取各项数据
                        for field in ['总资产', '股东权益合计', '实收资本(或股本)']:
                            if field in latest_balance:
                                try:
                                    value = latest_balance[field]
                                    # 确保值是数值型
                                    if pd.notna(value) and (isinstance(value, (int, float)) or (isinstance(value, str) and value.replace('.', '', 1).isdigit())):
                                        if isinstance(value, str):
                                            value = float(value)
                                        field_name = {
                                            '总资产': 'total_assets',
                                            '股东权益合计': 'net_assets',
                                            '实收资本(或股本)': 'total_shares'
                                        }.get(field)
                                        fundamental_data[field_name] = value
                                except Exception as e:
                                    print(f"处理 {symbol} 的 {field} 数据时出错: {e}")
                except Exception as e:
                    print(f"处理 {symbol} 资产负债表数据失败: {e}")
            
            # 2.2 获取利润表数据
            income_statement = None
            try:
                # 使用安全的方式获取利润表
                try:
                    income_statement = ak.stock_profit_sheet_by_report_em(symbol=stock_code)
                except Exception as e:
                    print(f"获取 {symbol} 利润表数据异常: {e}")
                    income_statement = None
                
                # 安全检查返回的数据
                if income_statement is None or not isinstance(income_statement, pd.DataFrame) or income_statement.empty:
                    print(f"获取 {symbol} 利润表数据返回无效结果")
                    income_statement = None
            except Exception as e:
                print(f"处理 {symbol} 利润表数据过程中发生错误: {e}")
                income_statement = None
            
            # 如果成功获取利润表，安全地提取数据
            if income_statement is not None:
                try:
                    # 确保至少有一行数据
                    if len(income_statement) > 0:
                        latest_income = income_statement.iloc[0]
                        
                        # 安全地获取净利润
                        if '净利润' in latest_income:
                            try:
                                value = latest_income['净利润']
                                # 确保值是数值型
                                if pd.notna(value) and (isinstance(value, (int, float)) or (isinstance(value, str) and value.replace('.', '', 1).replace('-', '', 1).isdigit())):
                                    if isinstance(value, str):
                                        value = float(value)
                                    fundamental_data['net_profit'] = value
                            except Exception as e:
                                print(f"处理 {symbol} 的净利润数据时出错: {e}")
                except Exception as e:
                    print(f"处理 {symbol} 利润表数据失败: {e}")
            
            # 3. 计算市盈率和市净率 - 使用更安全的方式
            try:
                # 安全地计算市值
                has_total_shares = ('total_shares' in fundamental_data and 
                                   pd.notna(fundamental_data.get('total_shares')) and 
                                   fundamental_data.get('total_shares') > 0)
                has_price = pd.notna(latest_price)
                
                if has_total_shares and has_price:
                    # 计算市值
                    try:
                        market_cap = latest_price * fundamental_data['total_shares']
                        fundamental_data['market_cap'] = market_cap
                        
                        # 安全地计算市盈率
                        has_net_profit = ('net_profit' in fundamental_data and 
                                         pd.notna(fundamental_data.get('net_profit')) and 
                                         fundamental_data.get('net_profit') > 0)
                        if has_net_profit:
                            try:
                                pe = market_cap / fundamental_data['net_profit']
                                if pd.notna(pe) and pe > 0 and pe < 1000:  # 添加合理性检查
                                    fundamental_data['pe'] = pe
                            except Exception as e:
                                print(f"计算 {symbol} 的市盈率时出错: {e}")
                        
                        # 安全地计算市净率
                        has_net_assets = ('net_assets' in fundamental_data and 
                                         pd.notna(fundamental_data.get('net_assets')) and 
                                         fundamental_data.get('net_assets') > 0)
                        if has_net_assets:
                            try:
                                pb = market_cap / fundamental_data['net_assets']
                                if pd.notna(pb) and pb > 0 and pb < 100:  # 添加合理性检查
                                    fundamental_data['pb'] = pb
                            except Exception as e:
                                print(f"计算 {symbol} 的市净率时出错: {e}")
                    except Exception as e:
                        print(f"计算 {symbol} 的市值时出错: {e}")
            except Exception as e:
                print(f"计算 {symbol} 的市盈率和市净率失败: {e}")
            
            # 4. 尝试获取ESG评级数据 - 使用更安全的方式
            try:
                # 安全地获取ESG数据
                esg_data = None
                try:
                    esg_data = ak.stock_esg_hz_sina()
                except Exception as e:
                    print(f"获取ESG评级数据异常: {e}")
                    esg_data = None
                
                # 安全检查返回的数据
                if esg_data is None or not isinstance(esg_data, pd.DataFrame) or esg_data.empty:
                    print(f"获取ESG评级数据返回无效结果")
                    esg_data = None
                
                # 如果成功获取ESG数据，安全地提取数据
                if esg_data is not None:
                    try:
                        # 查找当前股票的ESG评级
                        stock_esg = esg_data[esg_data['股票代码'] == symbol] if '股票代码' in esg_data.columns else pd.DataFrame()
                        
                        if not stock_esg.empty:
                            # 安全地获取各项ESG数据
                            for field in ['ESG评分', 'ESG等级', '环境', '社会', '公司治理']:
                                if field in stock_esg.columns:
                                    try:
                                        value = stock_esg.iloc[0][field]
                                        if pd.notna(value):
                                            field_name = {
                                                'ESG评分': 'esg_score',
                                                'ESG等级': 'esg_level',
                                                '环境': 'env_score',
                                                '社会': 'social_score',
                                                '公司治理': 'gov_score'
                                            }.get(field)
                                            fundamental_data[field_name] = value
                                    except Exception as e:
                                        print(f"处理 {symbol} 的 {field} ESG数据时出错: {e}")
                    except Exception as e:
                        print(f"处理 {symbol} ESG评级数据失败: {e}")
            except Exception as e:
                print(f"获取 {symbol} ESG评级数据失败: {e}")
            
            # 5. 尝试获取个股信息 - 使用更安全的方式
            try:
                # 安全地获取个股信息
                stock_info = None
                try:
                    stock_info = ak.stock_individual_info_em(symbol=stock_code)
                except Exception as e:
                    print(f"获取 {symbol} 个股信息异常: {e}")
                    stock_info = None
                
                # 安全检查返回的数据
                if stock_info is None or not isinstance(stock_info, pd.DataFrame) or stock_info.empty:
                    print(f"获取 {symbol} 个股信息返回无效结果")
                    stock_info = None
                
                # 如果成功获取个股信息，安全地提取数据
                if stock_info is not None:
                    try:
                        info_dict = {}
                        for _, row in stock_info.iterrows():
                            if 'item' in row and 'value' in row:
                                try:
                                    item = row['item']
                                    value = row['value']
                                    if pd.notna(item) and pd.notna(value):
                                        info_dict[item] = value
                                except Exception as e:
                                    print(f"处理 {symbol} 个股信息行时出错: {e}")
                        
                        # 安全地更新基本面数据
                        if info_dict:
                            fundamental_data.update(info_dict)
                    except Exception as e:
                        print(f"处理 {symbol} 个股信息失败: {e}")
            except Exception as e:
                print(f"获取 {symbol} 个股信息失败: {e}")
            
            # 保存到缓存
            try:
                pd.DataFrame([fundamental_data]).to_csv(cache_file)
            except Exception as e:
                print(f"保存 {symbol} 基本面数据到缓存失败: {e}")
            
            return fundamental_data
        except Exception as e:
            print(f"获取股票 {symbol} 基本面数据失败: {e}")
            return {}
    
    def select_stocks(self):
        """执行选股流程，返回选出的股票列表"""
        print(f"开始选股流程，时间范围: {self.start_date} 至 {self.end_date}")
        print(f"是否使用基本面数据: {'是' if self.use_fundamental else '否'}")
        
        # 存储所有股票的因子值
        all_factors = []
        
        # 记录处理成功和失败的股票数量
        success_count = 0
        fail_count = 0
        
        # 记录每个步骤的成功和失败数量
        step_stats = {
            'data_retrieval': {'success': 0, 'fail': 0},
            'tech_factors': {'success': 0, 'fail': 0},
            'fundamental_data': {'success': 0, 'fail': 0}
        }
        
        # 遍历股票池中的每只股票
        for i, symbol in enumerate(self.stocks):
            try:
                print(f"处理股票 {i+1}/{len(self.stocks)}: {symbol}")
                
                # 获取股票历史数据
                try:
                    df = self._get_stock_data(symbol, self.start_date, self.end_date)
                    if df.empty:
                        print(f"  - 无法获取 {symbol} 的历史数据，跳过")
                        step_stats['data_retrieval']['fail'] += 1
                        fail_count += 1
                        continue
                    step_stats['data_retrieval']['success'] += 1
                    print(f"  - 成功获取 {symbol} 的历史数据，共 {len(df)} 行")
                except Exception as e:
                    print(f"  - 获取 {symbol} 历史数据时发生错误: {e}")
                    step_stats['data_retrieval']['fail'] += 1
                    fail_count += 1
                    continue
                    
                # 计算技术因子
                try:
                    factors = self._calculate_factors(df)
                    if not factors:
                        print(f"  - 无法计算 {symbol} 的技术因子，跳过")
                        step_stats['tech_factors']['fail'] += 1
                        fail_count += 1
                        continue
                    step_stats['tech_factors']['success'] += 1
                    print(f"  - 成功计算 {symbol} 的技术因子，共 {len(factors)} 个因子")
                except Exception as e:
                    print(f"  - 计算 {symbol} 技术因子时发生错误: {e}")
                    step_stats['tech_factors']['fail'] += 1
                    fail_count += 1
                    continue
                    
                # 获取基本面数据
                if self.use_fundamental:
                    try:
                        fundamental_data = self._get_fundamental_data(symbol)
                        # 检查是否获取到了有用的基本面数据
                        useful_fields = ['pe', 'pb', 'market_cap', 'net_profit', 'total_assets', 'net_assets']
                        has_useful_data = fundamental_data and any(k in fundamental_data for k in useful_fields)
                        
                        if has_useful_data:
                            print(f"  - 成功获取 {symbol} 的基本面数据，包含以下字段:")
                            for field in useful_fields:
                                if field in fundamental_data:
                                    print(f"    * {field}: {fundamental_data[field]}")
                            # 合并因子和基本面数据
                            factors.update(fundamental_data)
                            step_stats['fundamental_data']['success'] += 1
                        else:
                            print(f"  - 获取 {symbol} 的基本面数据为空或不完整，仅使用技术因子")
                            step_stats['fundamental_data']['fail'] += 1
                    except Exception as e:
                        print(f"  - 获取或合并 {symbol} 基本面数据失败: {e}，将仅使用技术因子")
                        step_stats['fundamental_data']['fail'] += 1
                        # 继续使用技术因子
                    
                factors['symbol'] = symbol
                
                # 添加到结果列表
                all_factors.append(factors)
                success_count += 1
                print(f"  - 成功处理 {symbol}")
            except Exception as e:
                print(f"  - 处理 {symbol} 时发生错误: {e}")
                fail_count += 1
        
        print(f"\n处理完成: 成功 {success_count} 只股票，失败 {fail_count} 只股票")
        print(f"步骤统计:")
        print(f"  - 数据获取: 成功 {step_stats['data_retrieval']['success']} 只，失败 {step_stats['data_retrieval']['fail']} 只")
        print(f"  - 技术因子: 成功 {step_stats['tech_factors']['success']} 只，失败 {step_stats['tech_factors']['fail']} 只")
        if self.use_fundamental:
            print(f"  - 基本面数据: 成功 {step_stats['fundamental_data']['success']} 只，失败 {step_stats['fundamental_data']['fail']} 只")
        
        if not all_factors:
            print("没有足够的数据进行选股")
            return []
        
        # 转换为DataFrame
        try:
            factors_df = pd.DataFrame(all_factors)
            print(f"因子数据框大小: {factors_df.shape}")
            
            # 显示所有列名
            print(f"可用因子列: {list(factors_df.columns)}")
            
            # 检查是否有数值型列
            numeric_cols = factors_df.select_dtypes(include=[np.number]).columns.tolist()
            print(f"数值型因子列 ({len(numeric_cols)}): {numeric_cols}")
            
            if len(numeric_cols) <= 1:  # 只有symbol列不是数值型
                print("警告: 几乎没有数值型因子列，选股可能不准确")
                return []
        except Exception as e:
            print(f"创建因子数据框失败: {e}")
            return []
        
        # 移除包含太多缺失值的行
        try:
            before_rows = len(factors_df)
            # 计算每行的非缺失值比例
            non_na_ratio = factors_df.notna().sum(axis=1) / len(factors_df.columns)
            # 保留非缺失值比例大于70%的行
            factors_df = factors_df[non_na_ratio >= 0.7]
            after_rows = len(factors_df)
            print(f"移除缺失值后: 从 {before_rows} 行减少到 {after_rows} 行")
            
            if after_rows == 0:
                print("移除缺失值后没有足够的数据进行选股")
                return []
        except Exception as e:
            print(f"移除缺失值失败: {e}")
            if len(factors_df) == 0:
                return []
        
        # 对因子进行标准化处理
        try:
            numeric_cols = factors_df.select_dtypes(include=[np.number]).columns.tolist()
            if 'symbol' in numeric_cols:
                numeric_cols.remove('symbol')
                
            print(f"开始标准化 {len(numeric_cols)} 个数值型因子")
            standardized_cols = []
            skipped_cols = []
            
            for col in numeric_cols:
                try:
                    # 检查列是否有足够的非缺失值
                    non_na_count = factors_df[col].notna().sum()
                    if non_na_count < 2:
                        print(f"  - 列 '{col}' 几乎全是缺失值，跳过标准化")
                        skipped_cols.append(col)
                        continue
                        
                    # 使用中位数填充缺失值
                    median_value = factors_df[col].median()
                    factors_df[col] = factors_df[col].fillna(median_value)
                    
                    # 检查是否有常数列
                    if factors_df[col].std() == 0:
                        print(f"  - 列 '{col}' 是常数列，跳过标准化")
                        skipped_cols.append(col)
                        continue
                        
                    # Z-score标准化
                    mean_value = factors_df[col].mean()
                    std_value = factors_df[col].std()
                    factors_df[col] = (factors_df[col] - mean_value) / std_value
                    standardized_cols.append(col)
                except Exception as e:
                    print(f"  - 标准化列 '{col}' 时出错: {e}")
                    skipped_cols.append(col)
            
            print(f"因子标准化完成: 成功标准化 {len(standardized_cols)} 列，跳过 {len(skipped_cols)} 列")
            if standardized_cols:
                print(f"  - 标准化的列: {standardized_cols}")
            if skipped_cols:
                print(f"  - 跳过的列: {skipped_cols}")
                
            if not standardized_cols:
                print("没有可用的标准化因子，无法进行选股")
                return []
        except Exception as e:
            print(f"因子标准化失败: {e}")
            return []
        
        # 定义因子权重 - 可以根据实际需求调整
        factor_weights = {
            # 动量因子 - 正向关系
            'momentum_20d': 0.1,
            'momentum_60d': 0.1,
            
            # 波动率因子 - 负向关系
            'volatility_20d': -0.05,
            'volatility_60d': -0.05,
            
            # 技术指标因子
            'rsi_14': 0.05,  # RSI高表示强势
            'macd_hist': 0.1,  # MACD柱状图正值表示上涨趋势
            'bb_position': 0.05,  # 价格在布林带下方有上涨空间
            
            # 成交量因子
            'volume_change_20d': 0.05,  # 成交量增加表示活跃度提高
            
            # 趋势因子
            'price_to_ma20': 0.1,  # 价格高于均线表示上涨趋势
            'price_to_ma60': 0.1,  # 价格高于均线表示上涨趋势
            
            # 反转因子
            'reversion_5d': 0.05,  # 从低点反弹的幅度
            
            # 基本面因子 - 如果可用
            'pe': -0.05,  # 市盈率低更好
            'pb': -0.05,  # 市净率低更好
            'market_cap': 0.03,  # 市值适中偏大
            'total_assets': 0.02,  # 总资产规模
            'net_profit': 0.05,  # 净利润高更好
            
            # ESG因子 - 如果可用
            'esg_score': 0.03,  # ESG评分高更好
            'env_score': 0.01,  # 环境评分
            'social_score': 0.01,  # 社会评分
            'gov_score': 0.01,  # 公司治理评分
        }
        
        # 计算综合得分
        try:
            factors_df['score'] = 0
            used_factors = []
            
            print("\n计算综合得分:")
            for factor, weight in factor_weights.items():
                if factor in factors_df.columns and factor in standardized_cols:
                    try:
                        # 检查因子列是否有足够的非缺失值
                        non_na_count = factors_df[factor].notna().sum()
                        if non_na_count < len(factors_df) * 0.5:  # 如果超过一半是缺失值，跳过
                            print(f"  - 因子 '{factor}' 缺失值过多，不参与评分")
                            continue
                            
                        factors_df['score'] += factors_df[factor] * weight
                        used_factors.append((factor, weight))
                        print(f"  - 使用因子 '{factor}' 权重 {weight}")
                    except Exception as e:
                        print(f"  - 使用因子 '{factor}' 计算得分时出错: {e}")
            
            if not used_factors:
                print("没有可用的因子进行评分，无法进行选股")
                return []
                
            print(f"\n评分计算完成，使用了以下 {len(used_factors)} 个因子:")
            for factor, weight in used_factors:
                print(f"  - {factor}: 权重 {weight}")
        except Exception as e:
            print(f"计算综合得分失败: {e}")
            # 如果评分计算失败，使用第一个数值列作为得分
            try:
                score_col = standardized_cols[0] if standardized_cols else numeric_cols[0]
                factors_df['score'] = factors_df[score_col]
                print(f"使用 '{score_col}' 作为备选得分")
            except:
                print("无法创建备选得分，返回空结果")
                return []
        
        # 按得分排序
        try:
            # 确保得分列存在且有效
            if 'score' not in factors_df.columns or factors_df['score'].isna().all():
                print("得分列无效，无法排序")
                return []
                
            factors_df = factors_df.sort_values('score', ascending=False)
            print("按得分排序完成")
        except Exception as e:
            print(f"按得分排序失败: {e}")
            return []
        
        # 选择前N只股票
        try:
            top_n = min(self.top_n, len(factors_df))
            if top_n == 0:
                print("没有足够的股票进行选择")
                return []
                
            selected_stocks = factors_df.head(top_n)['symbol'].tolist()
            
            # 打印选出的股票及其得分
            print("\n选出的股票及其得分:")
            for i, (_, row) in enumerate(factors_df.head(top_n).iterrows(), 1):
                print(f"{i}. {row['symbol']}: {row['score']:.2f}")
        except Exception as e:
            print(f"选择前N只股票失败: {e}")
            selected_stocks = []
        
        print(f"\n选股完成，选出 {len(selected_stocks)} 只股票")
        return selected_stocks
    
    def save_selected_stocks(self, filename='selected_stocks.txt'):
        """保存选出的股票到文件"""
        selected_stocks = self.select_stocks()
        
        # 保存到文件
        with open(filename, 'w') as f:
            for stock in selected_stocks:
                f.write(f"{stock}\n")
        
        print(f"已将选出的股票保存到 {filename}")
        return selected_stocks


if __name__ == "__main__":
    # 使用示例
    selector = StockSelector(
        start_date='2023-01-01',
        end_date='2023-12-31',
        stock_pool='hs300',
        top_n=50,
        use_fundamental=True  # 设置是否使用基本面数据
    )
    
    # 执行选股并保存结果
    selected_stocks = selector.save_selected_stocks('selected_stocks.txt')
    
    # 打印选出的股票
    print("选出的股票:")
    for stock in selected_stocks:
        print(stock) 