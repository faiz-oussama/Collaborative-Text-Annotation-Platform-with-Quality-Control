import subprocess
import os
import sys
import time
import requests
import logging
import signal

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("service_starter.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Constants
SERVICE_URL = "http://localhost:5001"
MAX_RETRIES = 5
RETRY_INTERVAL = 2  # seconds

def check_service_health():
    """Check if the service is already running and healthy"""
    try:
        response = requests.get(f"{SERVICE_URL}/health", timeout=5)
        if response.status_code == 200:
            return True
    except requests.RequestException:
        pass
    return False

def start_service():
    """Start the spam detection service"""
    if check_service_health():
        logger.info("Spam detection service is already running")
        return True

    logger.info("Starting spam detection service...")
    
    # Get the directory of the current script
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Start the service as a subprocess
    try:
        process = subprocess.Popen(
            [sys.executable, os.path.join(script_dir, "__init__.py")],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            cwd=script_dir
        )
        
        # Wait for the service to start
        for attempt in range(MAX_RETRIES):
            logger.info(f"Waiting for service to start (attempt {attempt+1}/{MAX_RETRIES})...")
            time.sleep(RETRY_INTERVAL)
            
            if check_service_health():
                logger.info("Spam detection service started successfully!")
                return True
                
        logger.error("Failed to start spam detection service after multiple attempts")
        return False
        
    except Exception as e:
        logger.error(f"Error starting spam detection service: {str(e)}")
        return False

def stop_service():
    """Stop the spam detection service if it's running"""
    import psutil
    
    for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            cmdline = proc.info.get('cmdline', [])
            if len(cmdline) > 1 and '__init__.py' in cmdline[1] and 'python' in proc.info['name'].lower():
                logger.info(f"Stopping spam detection service (PID: {proc.info['pid']})")
                os.kill(proc.info['pid'], signal.SIGTERM)
                return True
        except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
            pass
    
    logger.info("No running spam detection service found")
    return False

if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description="Spam Detection Service Manager")
    parser.add_argument('action', choices=['start', 'stop', 'restart'], help='Action to perform')
    
    args = parser.parse_args()
    
    if args.action == 'start':
        start_service()
    elif args.action == 'stop':
        stop_service()
    elif args.action == 'restart':
        stop_service()
        time.sleep(1)
        start_service()
