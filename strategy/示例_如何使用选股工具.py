"""
选股工具使用示例
演示如何使用 simple_stock_picker.py 进行选股
"""

from simple_stock_picker import SimpleStockPicker, quick_pick


# ============ 方式1：最简单 - 一行代码 ============
def example_1_quick_pick():
    """一行代码快速选股"""
    print("【示例1】快速选股（推荐前20只）")
    quick_pick(top_n=20)


# ============ 方式2：自定义选股数量 ============
def example_2_custom_number():
    """自定义选股数量"""
    print("【示例2】自定义选股数量（推荐前10只）")
    
    picker = SimpleStockPicker()
    result = picker.run(top_n=10)
    
    if result is not None:
        print("\n✅ 选股完成！")


# ============ 方式3：进阶使用 - 自定义过滤条件 ============
def example_3_custom_filter():
    """进阶：自定义过滤条件"""
    print("【示例3】自定义过滤条件")
    
    picker = SimpleStockPicker()
    
    # 1. 获取股票列表
    stocks = picker.get_all_stocks()
    
    # 2. 基础过滤
    stocks = picker.filter_basic(stocks)
    
    # 3. 自定义过滤：只要涨幅在0-5%之间的
    stocks = stocks[(stocks['涨跌幅'] > 0) & (stocks['涨跌幅'] < 5)]
    
    # 4. 评分排名
    result = picker.rank_stocks(stocks, top_n=15)
    
    # 5. 格式化并显示
    output = picker.format_output(result)
    print("\n自定义过滤结果（涨幅0-5%）：")
    print(output.to_string(index=False))
    
    # 6. 保存结果
    picker.save_result(result, filename="自定义选股_0到5涨幅.csv")


# ============ 方式4：按行业选股 ============
def example_4_by_industry():
    """按行业筛选"""
    print("【示例4】按行业筛选")
    
    picker = SimpleStockPicker()
    stocks = picker.get_all_stocks()
    stocks = picker.filter_basic(stocks)
    
    # 如果有行业信息，可以按行业筛选
    # 注意：akshare的spot数据可能没有行业字段，需要用其他接口获取
    print("提示：需要额外获取行业数据")


# ============ 方式5：导出到Excel并分析 ============
def example_5_export_analysis():
    """导出结果并进行简单分析"""
    print("【示例5】导出并分析")
    
    picker = SimpleStockPicker()
    result = picker.run(top_n=30)
    
    if result is not None:
        # 统计分析
        print("\n" + "=" * 60)
        print("📊 统计分析")
        print("=" * 60)
        print(f"平均综合评分: {result['综合评分'].mean():.2f}")
        print(f"平均技术面评分: {result['技术面评分'].mean():.2f}")
        print(f"平均基本面评分: {result['基本面评分'].mean():.2f}")
        print(f"平均涨跌幅: {result['涨跌幅'].mean():.2f}%")
        
        # 导出到Excel
        try:
            import openpyxl
            filename = f"选股分析_{datetime.now().strftime('%Y%m%d')}.xlsx"
            result.to_excel(filename, index=False, engine='openpyxl')
            print(f"\n✅ 已导出到Excel: {filename}")
        except ImportError:
            print("\n⚠️ 未安装openpyxl，无法导出Excel")
            print("   可使用: pip install openpyxl")


# ============ 主菜单 ============
def main():
    """主菜单"""
    print("\n" + "=" * 60)
    print("🎯 选股工具使用示例")
    print("=" * 60)
    print("1. 快速选股（推荐前20只）")
    print("2. 自定义选股数量")
    print("3. 自定义过滤条件")
    print("4. 按行业选股")
    print("5. 导出并分析")
    print("0. 退出")
    print("=" * 60)
    
    choice = input("\n请选择示例 (0-5): ").strip()
    
    if choice == '1':
        example_1_quick_pick()
    elif choice == '2':
        example_2_custom_number()
    elif choice == '3':
        example_3_custom_filter()
    elif choice == '4':
        example_4_by_industry()
    elif choice == '5':
        example_5_export_analysis()
    elif choice == '0':
        print("👋 再见！")
    else:
        print("❌ 无效选择")


if __name__ == "__main__":
    # 默认运行方式1：快速选股
    example_1_quick_pick()
    
    # 如果想要交互式菜单，取消下面的注释
    # main()
