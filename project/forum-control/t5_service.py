"""
T5 Local Summarization Service (Python)
This Python script runs a local server that your Java app can call
"""

from transformers import T5Tokenizer, T5ForConditionalGeneration
from flask import Flask, request, jsonify
import sys

print("🤖 Loading T5-small model...")
print("📥 First run will download ~250MB")

# Load model and tokenizer
model_name = "google-t5/t5-small"
tokenizer = T5Tokenizer.from_pretrained(model_name)
model = T5ForConditionalGeneration.from_pretrained(model_name)

print("✅ Model loaded successfully!")

app = Flask(__name__)

@app.route('/summarize', methods=['POST'])
def summarize():
    """
    Endpoint to generate summaries
    POST /summarize
    Body: {"text": "your text here", "max_length": 130, "min_length": 30}
    """
    try:
        data = request.json
        text = data.get('text', '')
        max_length = data.get('max_length', 130)
        min_length = data.get('min_length', 30)

        if not text:
            return jsonify({'error': 'No text provided'}), 400

        # T5 requires task prefix
        input_text = f"summarize: {text}"

        # Tokenize
        inputs = tokenizer.encode(
            input_text,
            return_tensors="pt",
            max_length=512,
            truncation=True
        )

        # Generate summary
        summary_ids = model.generate(
            inputs,
            max_length=max_length,
            min_length=min_length,
            length_penalty=2.0,
            num_beams=4,
            early_stopping=True
        )

        # Decode
        summary = tokenizer.decode(summary_ids[0], skip_special_tokens=True)

        return jsonify({
            'summary': summary,
            'input_length': len(text),
            'output_length': len(summary)
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({'status': 'ready', 'model': model_name})

if __name__ == '__main__':
    print("\n🚀 T5 Summarization Service is running!")
    print("📍 Endpoint: http://localhost:5000/summarize")
    print("💡 Send POST requests with JSON: {\"text\": \"your text\"}")
    print("\n")
    app.run(host='0.0.0.0', port=5000, debug=False)