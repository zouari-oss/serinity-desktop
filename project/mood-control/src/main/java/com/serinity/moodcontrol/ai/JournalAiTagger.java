package com.serinity.moodcontrol.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JournalAiTagger {

    private static final String MODEL_RESOURCE_PATH = "/ai-models/journal-weka-smo-v1.model";
    public static final String MODEL_VERSION = "weka-smo-v1";

    // Must match training exactly (names + order).
    private static final List<String> LABELS = asLabels();

    // Always return top-K tags
    private static final int TOP_K = 3;

    // Storage rounding: 5 decimals avoids "0.0" for tiny but non-zero probs
    private static final int SCORE_DECIMALS = 5;

    // Optional: soften probabilities to avoid ultra-peaked outputs.
    // OFF by default because it changes the meaning of scores (ranking stays same).
    private static final boolean ENABLE_SOFTENING = false;
    private static final double SOFTEN_TEMPERATURE = 2.0; // >1.0 flattens more

    // Debug printing
    private static final boolean DEBUG = false;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Lazy-loaded model (thread-safe)
    private volatile Classifier model;

    // Header to classify new text
    private final Attribute textAttr;
    private final Instances header;

    public JournalAiTagger() {
        // Attribute 1: text (string)
        this.textAttr = new Attribute("text", (List<String>) null);

        // Attribute 2: label (nominal)
        Attribute labelAttr = new Attribute("label", new ArrayList<>(LABELS));

        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(textAttr);
        attrs.add(labelAttr);

        this.header = new Instances("journal_emotions_runtime", attrs, 0);
        this.header.setClassIndex(1);
    }

    /**
     * Returns JSON string suitable for ai_tags LONGTEXT column:
     * [{"tag":"stress","score":0.99397},{"tag":"fear","score":0.00598},{"tag":"anger","score":0.00005}]
     *
     * Always returns TOP_K (3) tags (unless text is blank -> "[]").
     */
    public String suggestTagsJson(final String journalText) {
        if (journalText == null || journalText.trim().isEmpty()) {
            return "[]";
        }

        try {
            double[] probs = distributionForText(journalText);

            if (ENABLE_SOFTENING) {
                probs = soften(probs, SOFTEN_TEMPERATURE);
            }

            if (DEBUG) {
                System.out.println("AI probs = " + Arrays.toString(probs));
            }

            // Build list of suggestions
            List<TagSuggestion> all = new ArrayList<>(LABELS.size());
            for (int i = 0; i < LABELS.size(); i++) {
                double p = (probs != null && i < probs.length) ? probs[i] : 0.0;
                all.add(new TagSuggestion(LABELS.get(i), p));
            }

            // Sort descending by score
            all.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            // Take top-K always
            int k = Math.min(TOP_K, all.size());
            List<TagSuggestion> kept = new ArrayList<>(k);
            for (int i = 0; i < k; i++) {
                kept.add(all.get(i));
            }

            // Convert to JSON
            List<Map<String, Object>> json = new ArrayList<>(kept.size());
            for (TagSuggestion s : kept) {
                Map<String, Object> obj = new LinkedHashMap<>();
                obj.put("tag", s.getTag());
                obj.put("score", roundN(s.getScore(), SCORE_DECIMALS));
                json.add(obj);
            }

            return MAPPER.writeValueAsString(json);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "[]";
        }
    }

    public LocalDateTime nowGeneratedAt() {
        return LocalDateTime.now();
    }

    // -------------------- internals --------------------

    private double[] distributionForText(final String text) throws Exception {
        Classifier m = getOrLoadModel();

        // Create a one-row dataset using the header
        Instances data = new Instances(header);
        DenseInstance inst = new DenseInstance(2);
        inst.setDataset(data);
        inst.setValue(textAttr, text);
        inst.setMissing(header.classAttribute()); // unknown label
        data.add(inst);

        return m.distributionForInstance(data.instance(0));
    }

    private Classifier getOrLoadModel() {
        Classifier m = this.model;
        if (m != null) return m;

        synchronized (this) {
            if (this.model != null) return this.model;

            try (InputStream in = JournalAiTagger.class.getResourceAsStream(MODEL_RESOURCE_PATH)) {
                if (in == null) {
                    throw new IllegalStateException("AI model not found in resources: " + MODEL_RESOURCE_PATH);
                }
                this.model = (Classifier) SerializationHelper.read(in);
                return this.model;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load WEKA model from " + MODEL_RESOURCE_PATH, ex);
            }
        }
    }

    /**
     * Temperature softening to reduce overconfident peaks.
     * Ranking stays the same, values become more spread when temperature > 1.0.
     */
    private static double[] soften(double[] probs, double temperature) {
        if (probs == null || probs.length == 0) return probs;

        double t = temperature <= 0.0 ? 1.0 : temperature;
        double power = 1.0 / t;

        double[] out = new double[probs.length];
        double sum = 0.0;

        for (int i = 0; i < probs.length; i++) {
            double p = probs[i];
            if (p < 0.0) p = 0.0;
            out[i] = Math.pow(p, power);
            sum += out[i];
        }

        if (sum <= 0.0) return probs;

        for (int i = 0; i < out.length; i++) {
            out[i] = out[i] / sum;
        }
        return out;
    }

    private static double roundN(double v, int decimals) {
        if (decimals <= 0) return Math.round(v);
        double m = Math.pow(10.0, decimals);
        return Math.round(v * m) / m;
    }

    private static List<String> asLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("joy");
        labels.add("sadness");
        labels.add("anger");
        labels.add("fear");
        labels.add("stress");
        labels.add("affection");
        labels.add("surprise");
        return labels;
    }
}