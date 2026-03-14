#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

APP_VERSION="1.0.0"
APP_JAR="app-${APP_VERSION}.jar"
OUT_DIR="app/target/native"
INPUT_DIR="app/target/dependency"
APP_DIR="${OUT_DIR}/Serinity"

echo "[deploy-native] Building all modules (tests skipped) and collecting runtime deps..."
mvn -pl app -am -Dmaven.test.skip=true package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency

echo "[deploy-native] Ensuring app and OpenCV jars are included in package input..."
cp -f "app/target/${APP_JAR}" "${INPUT_DIR}/"
cp -f "access-control/lib/opencv-4130.jar" "${INPUT_DIR}/"

echo "[deploy-native] Creating app image with jpackage..."
rm -rf "${APP_DIR}"
jpackage   --name Serinity   --input "${INPUT_DIR}"   --main-jar "${APP_JAR}"   --main-class com.serinity.app.Launcher   --type app-image   --dest "${OUT_DIR}"   --java-options "--enable-native-access=ALL-UNNAMED"

if [[ -f ".env.example" ]]; then
  cp -f ".env.example" "${APP_DIR}/.env.example"
elif [[ -f ".env.development" ]]; then
  cp -f ".env.development" "${APP_DIR}/.env.example"
fi

cat > "${APP_DIR}/Serinity" << 'EOF'
#!/usr/bin/env bash
set -euo pipefail
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$APP_DIR"
if [[ ! -f ".env" ]]; then
  echo "[Serinity] Missing .env in $APP_DIR"
  echo "[Serinity] Create it from .env.example before launching."
  exit 1
fi
set -a
source "$APP_DIR/.env"
set +a
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dserinity.env.dir=$APP_DIR"
exec "$APP_DIR/bin/Serinity" "$@"
EOF
chmod +x "${APP_DIR}/Serinity"

echo "[deploy-native] Done."
echo "[deploy-native] Start app with: ${APP_DIR}/Serinity"
