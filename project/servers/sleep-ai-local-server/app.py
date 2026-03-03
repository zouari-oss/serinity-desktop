from flask import Flask, request, jsonify
from llama_cpp import Llama
import json, re

app = Flask(__name__)

# Charge ton modèle GGUF local
llm = Llama(
    model_path="Mistral-7B-Instruct-v0.3-Q4_K_M.gguf",
    n_ctx=4096,
    n_threads=8,  # adapte selon ton CPU
    n_gpu_layers=0,  # mets >0 si tu as GPU
)


def extraire_json(texte):
    try:
        return json.loads(texte)
    except:
        pass
    match = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", texte, re.DOTALL)
    if match:
        return json.loads(match.group(1))
    match = re.search(r"\{.*\}", texte, re.DOTALL)
    if match:
        return json.loads(match.group(0))
    raise ValueError("Aucun JSON valide trouvé")


def generer_reponse(prompt):
    response = llm.create_chat_completion(
        messages=[
            {
                "role": "system",
                "content": "Tu es un psychologue clinicien spécialisé en analyse des rêves. Réponds uniquement en JSON valide.",
            },
            {"role": "user", "content": prompt},
        ],
        temperature=0.3,
    )
    return response["choices"][0]["message"]["content"]


@app.route("/analyser-reve", methods=["POST"])
def analyser_reve():
    data = request.json

    prompt = f"""
Analyse ce rêve et retourne UNIQUEMENT ce JSON valide :

{{
  "scorePsychologique": <entier entre 0 et 100>,
  "profilDominant": <"ANXIEUX","CRÉATIF","NOSTALGIQUE","CONFLICTUEL","SPIRITUEL","INSÉCURISÉ","LIBÉRATEUR","ÉQUILIBRÉ">,
  "symbolesDetectes": {{ "nom_symbole": "signification psychologique" }},
  "impactEmotionnel": <emoji — description>,
  "conclusion": <2 à 3 phrases>,
  "recommandations": ["conseil 1","conseil 2","conseil 3","conseil 4"],
  "niveauAlerte": <"AUCUN","FAIBLE","MODÉRÉ","ÉLEVÉ","CRITIQUE">
}}

Données :
Titre: {data.get("titre")}
Description: {data.get("description")}
Émotions: {data.get("emotions")}
Type: {data.get("typeReve")}
Intensité: {data.get("intensite")}/10
Couleur: {data.get("couleur")}
Récurrent: {data.get("recurrent")}
"""

    try:
        content = generer_reponse(prompt)
        parsed = extraire_json(content)
        return jsonify({"result": json.dumps(parsed, ensure_ascii=False)})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ✅ ON GARDE CETTE ROUTE EXACTEMENT
@app.route("/analyser-tous", methods=["POST"])
def analyser_tous():
    reves = request.json.get("reves", [])

    if not reves:
        return jsonify(
            {
                "result": json.dumps(
                    {"scorePsychologique": 50, "conclusion": "Aucun rêve enregistré."}
                )
            }
        )

    resume = "\n".join(
        [
            f"- [{r.get('typeReve', '?')}] {r.get('titre', '?')} | émotions: {r.get('emotions', '?')} | intensité: {r.get('intensite', '?')}/10"
            for r in reves
        ]
    )

    prompt = f"""
Analyse globale de {len(reves)} rêves.

Retourne UNIQUEMENT ce JSON valide :

{{
  "scorePsychologique": <moyenne 0-100>,
  "profilDominant": <"ANXIEUX","CRÉATIF","NOSTALGIQUE","CONFLICTUEL","SPIRITUEL","INSÉCURISÉ","LIBÉRATEUR","ÉQUILIBRÉ">,
  "symbolesDetectes": {{ "symbole (×N)": "signification" }},
  "impactEmotionnel": <emoji — description>,
  "conclusion": <2 à 3 phrases>,
  "recommandations": ["conseil 1","conseil 2","conseil 3","conseil 4","conseil 5"],
  "niveauAlerte": <"AUCUN","FAIBLE","MODÉRÉ","ÉLEVÉ","CRITIQUE">
}}

Rêves :
{resume}
"""

    try:
        content = generer_reponse(prompt)
        parsed = extraire_json(content)
        return jsonify({"result": json.dumps(parsed, ensure_ascii=False)})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(port=5000, debug=True)
