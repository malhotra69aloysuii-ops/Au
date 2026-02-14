#!/bin/bash

# Chrome and Selenium Auto Setup Script for Ubuntu
# Pure bash script - no Python code embedded

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_message() {
    echo -e "${2}${1}${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_message "This script should not be run as root" "$RED"
   exit 1
fi

print_message "======================================" "$GREEN"
print_message "Chrome & Selenium Auto Setup Script" "$GREEN"
print_message "======================================" "$GREEN"

# Update package list
print_message "\n[1/7] Updating package list..." "$YELLOW"
sudo apt-get update

# Install required dependencies
print_message "\n[2/7] Installing required dependencies..." "$YELLOW"
sudo apt-get install -y wget curl unzip python3 python3-pip python3-venv

# Create temporary directory
TEMP_DIR=$(mktemp -d)
cd "$TEMP_DIR"

# Download Chrome
print_message "\n[3/7] Downloading Chrome..." "$YELLOW"
CHROME_DEB="google-chrome-stable_current_amd64.deb"
wget -O "$CHROME_DEB" "https://www.dropbox.com/scl/fi/ku84be0mhsr1j0rq5ij6m/google-chrome-stable_current_amd64.deb?rlkey=xgbyb269podvqtecaid19ix8q&st=id30yuc9&dl=1"

# Install Chrome
print_message "\n[4/7] Installing Chrome..." "$YELLOW"
sudo dpkg -i "$CHROME_DEB" || sudo apt-get install -f -y

# Get Chrome version
print_message "\n[5/7] Detecting Chrome version..." "$YELLOW"
CHROME_VERSION=$(google-chrome --version | awk '{print $3}')
CHROME_MAJOR_VERSION=$(echo $CHROME_VERSION | cut -d'.' -f1)

print_message "Detected Chrome version: $CHROME_VERSION" "$GREEN"
print_message "Major version: $CHROME_MAJOR_VERSION" "$GREEN"

# Create project structure
print_message "\n[6/7] Setting up Python environment..." "$YELLOW"
cd ~
mkdir -p selenium_project
cd selenium_project

# Create virtual environment
python3 -m venv selenium_env

# Create requirements file
cat > requirements.txt << 'EOF'
selenium
webdriver-manager
EOF

# Create activation script
cat > activate.sh << 'EOF'
#!/bin/bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$DIR/selenium_env/bin/activate"
echo "✓ Selenium environment activated"
echo "  Run: python test_selenium.py"
EOF
chmod +x activate.sh

# Create test script
cat > test_selenium.py << 'EOF'
#!/usr/bin/env python3
"""
Test script for Selenium setup
"""
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import sys

def test_chrome():
    print("Testing Chrome installation...")
    try:
        # Setup Chrome options
        options = webdriver.ChromeOptions()
        options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        
        # Initialize driver
        print("Downloading ChromeDriver (if needed)...")
        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service, options=options)
        
        # Test navigation
        print("Testing navigation...")
        driver.get('https://www.google.com')
        print(f"✓ Success! Page title: {driver.title}")
        
        driver.quit()
        return True
    except Exception as e:
        print(f"✗ Test failed: {e}")
        return False

if __name__ == "__main__":
    success = test_chrome()
    sys.exit(0 if success else 1)
EOF

# Install Python packages
print_message "\n[7/7] Installing Python packages..." "$YELLOW"
source selenium_env/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

# Run test
print_message "\nRunning Selenium test..." "$YELLOW"
if python test_selenium.py; then
    print_message "✓ Selenium test passed!" "$GREEN"
else
    print_message "✗ Selenium test failed!" "$RED"
fi

# Create example script
cat > example.py << 'EOF'
#!/usr/bin/env python3
"""
Example Selenium script for web automation
"""
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time

def main():
    print("=" * 50)
    print("Selenium Example Script")
    print("=" * 50)
    
    # Setup Chrome options
    options = webdriver.ChromeOptions()
    options.add_argument('--start-maximized')
    # options.add_argument('--headless')  # Uncomment for headless mode
    
    # Initialize driver
    print("\n1. Starting Chrome browser...")
    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=options)
    
    try:
        # Navigate to website
        print("2. Navigating to example.com...")
        driver.get("https://www.example.com")
        
        # Wait for page to load
        wait = WebDriverWait(driver, 10)
        heading = wait.until(
            EC.presence_of_element_located((By.TAG_NAME, "h1"))
        )
        
        # Get page information
        print("3. Page loaded successfully!")
        print(f"   Title: {driver.title}")
        print(f"   Heading: {heading.text}")
        
        # Take screenshot
        screenshot = "example.png"
        driver.save_screenshot(screenshot)
        print(f"4. Screenshot saved: {screenshot}")
        
        # Get page source length
        print(f"5. Page source length: {len(driver.page_source)} characters")
        
        # Execute JavaScript
        url = driver.execute_script("return window.location.href")
        print(f"6. Current URL: {url}")
        
        time.sleep(2)
        
    except Exception as e:
        print(f"Error: {e}")
    finally:
        # Close browser
        print("7. Closing browser...")
        driver.quit()
    
    print("\n✓ Example completed successfully!")

if __name__ == "__main__":
    main()
EOF

# Create README
cat > README.txt << EOF
Selenium Project Setup
=====================
Chrome Version: $CHROME_VERSION
Setup Date: $(date)

Files:
- selenium_env/    : Python virtual environment
- test_selenium.py : Quick test script
- example.py      : Example automation script
- requirements.txt: Python dependencies
- activate.sh     : Script to activate environment

Usage:
1. Activate environment: source ./activate.sh
2. Run test:           python test_selenium.py
3. Run example:        python example.py

To install additional packages:
  pip install package_name
EOF

# Cleanup
print_message "\nCleaning up temporary files..." "$YELLOW"
rm -rf "$TEMP_DIR"

# Deactivate virtual environment
deactivate 2>/dev/null || true

# Final output
print_message "\n" "$NC"
print_message "======================================" "$GREEN"
print_message "✅ SETUP COMPLETE!" "$GREEN"
print_message "======================================" "$NC"
print_message "\n📦 Chrome Version: $CHROME_VERSION" "$GREEN"
print_message "📁 Project Location: ~/selenium_project" "$GREEN"
print_message "" "$NC"
print_message "Next steps:" "$YELLOW"
print_message "1. cd ~/selenium_project" "$NC"
print_message "2. source ./activate.sh" "$NC"
print_message "3. python test_selenium.py  # Verify setup" "$NC"
print_message "4. python example.py         # Run example" "$NC"
print_message "" "$NC"
print_message "For help: cat ~/selenium_project/README.txt" "$YELLOW"
print_message "======================================" "$GREEN"
