import tkinter as tk
from tkinter import messagebox

class Calculator:
    def __init__(self, root):
        self.root = root
        self.root.title("简单计算器")
        
        # 创建显示结果的输入框
        self.display = tk.Entry(root, width=30, justify="right")
        self.display.grid(row=0, column=0, columnspan=4, padx=5, pady=5)
        
        # 按钮布局
        buttons = [
            '7', '8', '9', '/',
            '4', '5', '6', '*',
            '1', '2', '3', '-',
            '0', '.', '=', '+'
        ]
        
        # 创建按钮
        row = 1
        col = 0
        for button in buttons:
            cmd = lambda x=button: self.click(x)
            tk.Button(root, text=button, width=7, height=2, command=cmd).grid(row=row, column=col)
            col += 1
            if col > 3:
                col = 0
                row += 1
        
        # 添加清除按钮
        tk.Button(root, text='清除', width=7, height=2, command=self.clear).grid(row=row, column=col)

    def click(self, key):
        if key == '=':
            try:
                result = eval(self.display.get())
                self.display.delete(0, tk.END)
                self.display.insert(tk.END, str(result))
            except:
                messagebox.showerror("错误", "计算表达式错误")
                self.display.delete(0, tk.END)
        else:
            self.display.insert(tk.END, key)

    def clear(self):
        self.display.delete(0, tk.END)

if __name__ == "__main__":
    root = tk.Tk()  # 修改这里：使用 Tk() 而不是 Window()
    calculator = Calculator(root)
    root.mainloop()