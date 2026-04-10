#!/usr/bin/env bash

PROJECT_DIR="/home/blue/authvault-android"
cd "$PROJECT_DIR"
DIST_DIR="$PROJECT_DIR/dist"
GRADLEW="$PROJECT_DIR/gradlew"
BUILD_TYPE="${1:-debug}"

echo "============================================"
echo "  AuthVault Build Script"
echo "============================================"
echo "  Build Type : $BUILD_TYPE"
echo "  Output Dir : $DIST_DIR"
echo "============================================"
echo ""

if [ ! -f "$GRADLEW" ]; then
    echo "ERROR: gradlew not found at $GRADLEW"
    exit 1
fi

chmod +x "$GRADLEW"

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

case "$BUILD_TYPE" in
    debug)
        echo "[1/3] Cleaning previous builds..."
        "$GRADLEW" --no-configuration-cache clean > /tmp/build.log 2>&1
        echo "      Done."

        echo "[2/3] Building Debug APKs (split ABI)..."
        if "$GRADLEW" --no-configuration-cache assembleDebug >> /tmp/build.log 2>&1; then
            echo "      Build successful."
        else
            echo "      Build failed! Check /tmp/build.log for details."
            tail -20 /tmp/build.log
            exit 1
        fi

        echo "[3/3] Copying APKs to $DIST_DIR..."
        cp "$PROJECT_DIR"/app/build/outputs/apk/debug/*.apk "$DIST_DIR/"
        ;;

    release)
        KEYSTORE_FILE="$PROJECT_DIR/app/release.keystore"
        if [ ! -f "$KEYSTORE_FILE" ]; then
            echo "WARNING: release.keystore not found. Generating one..."
            keytool -genkey -v \
                -keystore "$KEYSTORE_FILE" \
                -alias authvault \
                -keyalg RSA \
                -keysize 2048 \
                -validity 10000 \
                -storepass authvault \
                -keypass authvault \
                -dname "CN=AuthVault, OU=Dev, O=AuthVault, L=City, S=State, C=US"
        fi

        echo "[1/4] Cleaning previous builds..."
        "$GRADLEW" --no-configuration-cache clean > /tmp/build.log 2>&1
        echo "      Done."

        echo "[2/4] Building Release APKs (split ABI + minify)..."
        export KEYSTORE_PASSWORD="authvault"
        export KEY_ALIAS="authvault"
        export KEY_PASSWORD="authvault"
        if "$GRADLEW" --no-configuration-cache assembleRelease >> /tmp/build.log 2>&1; then
            echo "      Build successful."
        else
            echo "      Build failed! Check /tmp/build.log for details."
            tail -20 /tmp/build.log
            exit 1
        fi

        echo "[3/4] Copying APKs to $DIST_DIR..."
        cp "$PROJECT_DIR"/app/build/outputs/apk/release/*.apk "$DIST_DIR/"
        ;;

    all)
        echo "[1/6] Cleaning previous builds..."
        "$GRADLEW" --no-configuration-cache clean > /tmp/build.log 2>&1
        echo "      Done."

        echo "[2/6] Building Debug APKs..."
        if "$GRADLEW" --no-configuration-cache assembleDebug >> /tmp/build.log 2>&1; then
            echo "      Debug build successful."
        else
            echo "      Debug build failed! Check /tmp/build.log."
            tail -20 /tmp/build.log
            exit 1
        fi

        echo "[3/6] Copying Debug APKs to $DIST_DIR/debug/..."
        mkdir -p "$DIST_DIR/debug"
        cp "$PROJECT_DIR"/app/build/outputs/apk/debug/*.apk "$DIST_DIR/debug/"

        echo "[4/6] Building Release APKs..."
        KEYSTORE_FILE="$PROJECT_DIR/app/release.keystore"
        if [ ! -f "$KEYSTORE_FILE" ]; then
            keytool -genkey -v \
                -keystore "$KEYSTORE_FILE" \
                -alias authvault \
                -keyalg RSA \
                -keysize 2048 \
                -validity 10000 \
                -storepass authvault \
                -keypass authvault \
                -dname "CN=AuthVault, OU=Dev, O=AuthVault, L=City, S=State, C=US"
        fi
        export KEYSTORE_PASSWORD="authvault"
        export KEY_ALIAS="authvault"
        export KEY_PASSWORD="authvault"
        if "$GRADLEW" --no-configuration-cache assembleRelease >> /tmp/build.log 2>&1; then
            echo "      Release build successful."
        else
            echo "      Release build failed! Check /tmp/build.log."
            tail -20 /tmp/build.log
            exit 1
        fi

        echo "[5/6] Copying Release APKs to $DIST_DIR/release/..."
        mkdir -p "$DIST_DIR/release"
        cp "$PROJECT_DIR"/app/build/outputs/apk/release/*.apk "$DIST_DIR/release/"
        ;;

    lint)
        echo "[1/2] Running lint checks..."
        "$GRADLEW" --no-configuration-cache lintDebug 2>&1 | tail -10

        echo "[2/2] Running unit tests..."
        "$GRADLEW" --no-configuration-cache testDebugUnitTest 2>&1 | tail -10
        ;;

    *)
        echo "Usage: $0 {debug|release|all|lint}"
        echo ""
        echo "  debug   - Build debug APKs (split ABI)"
        echo "  release - Build release APKs (split ABI, minified)"
        echo "  all     - Build both debug and release"
        echo "  lint    - Run lint checks and unit tests"
        exit 1
        ;;
esac

echo ""
echo "============================================"
echo "  Build Complete!"
echo "============================================"

if [ "$BUILD_TYPE" = "all" ]; then
    echo "  Debug APKs  : $DIST_DIR/debug/"
    echo "  Release APKs: $DIST_DIR/release/"
else
    echo "  Output Directory: $DIST_DIR/"
fi

echo ""
echo "  Generated APKs:"
if [ "$BUILD_TYPE" = "all" ]; then
    find "$DIST_DIR" -name "*.apk" -exec ls -lh {} \;
else
    ls -lh "$DIST_DIR"/*.apk 2>/dev/null || echo "  (no APKs found)"
fi

echo ""
echo "============================================"
