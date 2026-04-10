# Build Scripts

## Usage

```bash
# Build debug APKs
./scripts/build.sh debug

# Build release APKs (auto-generates keystore if missing)
./scripts/build.sh release

# Build both debug and release
./scripts/build.sh all

# Run lint checks and unit tests
./scripts/build.sh lint
```

## Output

All APKs are copied to `/home/blue/authvault-android/dist/` automatically.

| Build Type | Output Location |
|------------|-----------------|
| `debug` | `dist/*.apk` |
| `release` | `dist/*.apk` |
| `all` | `dist/debug/*.apk` + `dist/release/*.apk` |

## CI/CD

GitHub Actions workflow: `.github/workflows/build-release.yml`

The CI/CD pipeline:
1. Validates code (lint + tests)
2. Builds debug APKs on every push/PR
3. Builds release APKs (split ABI) on push to `main`
4. Creates GitHub Release with APKs attached
