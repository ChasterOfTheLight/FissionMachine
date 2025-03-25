# FissionMachine Strategy Setup Guide

## Environment Preparation

1. Create virtual environment
```bash
python -m venv venv
```

2. Activate virtual environment
- Windows:
```bash
.\venv\Scripts\activate
```
- Linux/Mac:
```bash
source venv/bin/activate
```

## Dependencies Management

1. Generate requirements.txt (if you have installed packages)
```bash
pip freeze > requirements.txt
```

2. Install dependencies
```bash
pip install -r requirements.txt
```

## Development Setup

1. Make sure you have activated the virtual environment (you should see `(venv)` in your terminal)
2. Install any new dependencies through pip
3. Remember to update requirements.txt when adding new packages:
```bash
pip freeze > requirements.txt
```
