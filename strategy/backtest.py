import pandas as pd
import akshare as ak
import logging
from my_strategy import stock_recommendation_strategy, filter_stocks, get_stock_data
from datetime import datetime, timedelta
import matplotlib.pyplot as plt


def backtest(start_date, end_date):
    """
    回测策略
    :param start_date: 回测开始日期，格式：'20240212'
    :param end_date: 回测结束日期，格式：'20240219'
    """
    # 计算需要往前推的日期以确保有足够的数据计算MA
    start_date_obj = datetime.strptime(start_date, '%Y%m%d')
    adjusted_start = (start_date_obj - timedelta(days=30)).strftime('%Y%m%d')
    
    logging.info(f"调整后的开始日期: {adjusted_start} (为确保MA计算)")
    logging.info(f"结束日期: {end_date}")

    # 获取股票池
    stock_list = filter_stocks()
    logging.info(f"股票池数量: {len(stock_list)}")

    # 存储回测结果
    results = []

    # 获取每只股票的数据并进行策略评估
    for stock in stock_list[:300]:  # 先测试前300只股票
        try:
            # 获取更长时间段的历史数据用于计算MA
            stock_data = get_stock_data(stock, adjusted_start, end_date)
            if stock_data.empty or len(stock_data) < 11:  # 确保至少有11天数据用于计算MA10
                logging.warning(f"股票 {stock} 数据不足，跳过")
                continue

            # 运行策略
            recommendation = stock_recommendation_strategy(stock_data)

            # 检查MA值是否有效
            if pd.isna(recommendation['MA5']) or pd.isna(recommendation['MA10']):
                logging.warning(f"股票 {stock} MA值无效，跳过")
                continue

            if recommendation['Score'] >= 0.8:
                # 获取推荐当天的收盘价
                entry_price = recommendation['收盘']
                # 获取之后的交易日数据
                future_data = stock_data[stock_data['日期'] > recommendation['日期']]
                
                # 如果是最新日期的推荐，也记录下来
                if future_data.empty:
                    results.append({
                        '股票代码': stock,
                        '推荐日期': recommendation['日期'],
                        '推荐评分': recommendation['Score'],
                        '入场价': entry_price,
                        '5日最高': entry_price,  # 使用当前价格
                        '潜在收益率': 0,  # 标记为0
                        '是否最新推荐': True  # 添加标记
                    })
                else:
                    max_future_price = future_data['收盘'].max()
                    potential_return = (max_future_price - entry_price) / entry_price * 100

                    results.append({
                        '股票代码': stock,
                        '推荐日期': recommendation['日期'],
                        '推荐评分': recommendation['Score'],
                        '入场价': entry_price,
                        '5日最高': max_future_price,
                        '潜在收益率': potential_return,
                        '是否最新推荐': False
                    })

            logging.info(f"完成股票 {stock} 的回测分析")

        except Exception as e:
            logging.error(f"处理股票 {stock} 时出错: {e}")
            continue

    # 转换为DataFrame并分析结果
    results_df = pd.DataFrame(results)
    if not results_df.empty:
        # 分开显示历史回测和最新推荐
        historical_df = results_df[~results_df['是否最新推荐']]
        latest_df = results_df[results_df['是否最新推荐']]
        
        logging.info("\n回测结果汇总:")
        if not historical_df.empty:
            logging.info("历史回测结果:")
            logging.info(f"历史推荐股票数量: {len(historical_df)}")
            logging.info(f"平均潜在收益率: {historical_df['潜在收益率'].mean():.2f}%")
            logging.info(f"最大潜在收益率: {historical_df['潜在收益率'].max():.2f}%")
            logging.info(f"最小潜在收益率: {historical_df['潜在收益率'].min():.2f}%")
        
        if not latest_df.empty:
            logging.info("\n最新推荐股票:")
            logging.info(f"数量: {len(latest_df)}")
            logging.info("\n具体推荐:")
            logging.info(latest_df[['股票代码', '推荐日期', '推荐评分', '入场价']].to_string())
        
        # 保存结果到CSV
        results_df.to_csv(
            f'backtest_results_{start_date}_{end_date}.csv', index=False)

        # 绘制收益分布图
        plt.figure(figsize=(10, 6))
        plt.hist(results_df['潜在收益率'], bins=20)
        plt.title('策略收益分布')
        plt.xlabel('潜在收益率(%)')
        plt.ylabel('频率')
        plt.savefig(f'backtest_distribution_{start_date}_{end_date}.png')
        plt.close()

    return results_df


if __name__ == "__main__":
    # 设置日志
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )

    # 执行回测
    start_date = '20250112'
    end_date = '20250212'
    results = backtest(start_date, end_date)

    if not results.empty:
        # 显示详细结果
        print("\n推荐股票详细信息:")
        print(results.sort_values(by='潜在收益率', ascending=False))
