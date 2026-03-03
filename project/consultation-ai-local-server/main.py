from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI(title="Clinical Triage AI")

# ===== MEDICAL ZERO SHOT CLASSIFIER =====
# Ce modèle comprend beaucoup mieux les symptômes
triage_model = pipeline(
    "zero-shot-classification",
    model="facebook/bart-large-mnli"
)

# (beaucoup plus intelligent que regex)

class PredictRequest(BaseModel):
    text: str

# hypothèses médicales
LABELS = [
    "medical emergency requiring immediate attention",
    "needs doctor consultation soon",
    "minor symptoms self care at home"
]

def triage_decision(text: str):

    result = triage_model(
        text,
        LABELS,
        multi_label=False
    )

    label = result["labels"][0]
    score = result["scores"][0]

    if label == LABELS[0]:
        urgency = "HIGH"
    elif label == LABELS[1]:
        urgency = "MEDIUM"
    else:
        urgency = "LOW"

    return urgency, score

def recommendation_for(urgency):
    if urgency == "HIGH":
        return (
            "⚠ URGENCE MÉDICALE POSSIBLE.\n"
            "Appelez immédiatement les urgences.\n"
            "Ne restez pas seul.\n"
            "Symptômes potentiellement graves."
        )
    elif urgency == "MEDIUM":
        return (
            "Consultez un médecin rapidement (24h).\n"
            "Surveillez l'évolution des symptômes."
        )
    else:
        return (
            "Symptômes probablement bénins.\n"
            "Repos, hydratation et surveillance."
        )

@app.post("/predict")
def predict(req: PredictRequest):

    text = req.text

    urgency, confidence = triage_decision(text)

    return {
        "urgency": urgency,
        "confidence": round(confidence, 3),
        "recommendation": recommendation_for(urgency)
    }