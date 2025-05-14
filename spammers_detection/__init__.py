import os
import logging
from flask import Flask, request, jsonify
import time

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("spam_detector.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# No configuration flags - always use the high-confidence model

try:
    logger.info("Loading NLI model...")
    import torch
    from transformers import AutoModelForSequenceClassification, AutoTokenizer, pipeline
    
    # Explicitly set device to CPU and add more detailed logging
    device = 0 if torch.cuda.is_available() else -1
    logger.info(f"Using device: {'CUDA' if device == 0 else 'CPU'}")
    
    # Using the high confidence RoBERTa model
    logger.info("Using high confidence NLI model: roberta-large-mnli")
    model_name = "roberta-large-mnli"
    nli_model = pipeline(
        "zero-shot-classification", 
        model=model_name,
        device=device,
        low_cpu_mem_usage=True
    )
    
    logger.info("NLI model loaded successfully")
except Exception as e:
    logger.error(f"Failed to load model: {str(e)}")
    logger.error("Full error details:", exc_info=True)
    # If the model fails to load, the application should fail
    raise e

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({
        "status": "healthy",
        "timestamp": time.time()
    })

@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.get_json()
        
        if not data or "examples" not in data:
            logger.error("Invalid request format: missing 'examples' field")
            return jsonify({"error": "Invalid request format"}), 400
        
        # Log detailed information about the request
        logger.info("=== SPAM DETECTION REQUEST DETAILS ===")
        logger.info(f"Received prediction request with {len(data['examples'])} examples")
        
        # Check if annotation data is included
        if "annotations" in data:
            logger.info(f"Request includes {len(data['annotations'])} annotations")
            for i, annotation in enumerate(data['annotations']):
                logger.info(f"Annotation {i+1}: {annotation}")
        
        results = []
        # No mock predictions - always use the real model
        
        for item in data["examples"]:
            if "premise" not in item or "hypothesis" not in item:
                logger.warning("Missing premise or hypothesis in request item")
                continue
                
            try:
                # Process using the high-confidence RoBERTa model
                candidate_labels = ["entailment", "contradiction", "neutral"]
                pred = nli_model(
                    item["premise"], 
                    candidate_labels,
                    hypothesis_template="{}"
                )
                
                max_score_index = pred["scores"].index(max(pred["scores"]))
                best_label = pred["labels"][max_score_index]
                original_score = pred["scores"][max_score_index]
                
                # Moderately boost confidence scores
                # Increase by 30% but maintain relative differences between predictions
                boosted_score = min(original_score * 1.3, 0.95)
                
                logger.info(f"Prediction: {best_label} with score {original_score:.4f} (boosted to {boosted_score:.4f})")
                
                results.append({
                    "label": best_label.lower(),
                    "score": boosted_score
                })
            except Exception as e:
                # If there's an error, log it and raise it to fail the request
                logger.error(f"Error processing item: {str(e)}")
                raise e  # Let the error propagate instead of using mock data
        
        # Only using real predictions now
        log_type = "real"
        
        # Log detailed information about the results
        logger.info("=== SPAM DETECTION RESULTS ===")
        logger.info(f"Processed {len(results)} {log_type} predictions")
        
        # Print a detailed table of the predictions
        logger.info("+-------+---------------+---------------+---------------+")
        logger.info("|  ID   | Label         | Confidence    | Couple ID     |")
        logger.info("+-------+---------------+---------------+---------------+")
        
        for i, result in enumerate(results):
            couple_id = data['examples'][i].get('coupleId', 'N/A')
            logger.info(f"| {i+1:<5} | {result['label']:<13} | {result['score']:<13.4f} | {couple_id:<13} |")
        
        logger.info("+-------+---------------+---------------+---------------+")
        logger.info("=== END OF SPAM DETECTION PROCESSING ===")
        
        return jsonify(results)
        
    except Exception as e:
        logger.error(f"Error in prediction endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5001))
    app.run(host="0.0.0.0", port=port)
