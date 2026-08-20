# Android internal-track release

The `Android internal release` GitHub Actions workflow runs after Android or curriculum changes
land on `main`. It gates publishing on JVM tests, release lint, and a signed Android App Bundle,
then publishes that bundle to Google Play's `internal` track. It can also be started manually.

## One-time Play Console setup

1. Create `dev.novanest.droidquest` in Play Console and enrol it in Play App Signing.
2. Upload the first signed bundle manually if the package has never had a Play release. Google Play
   requires the application and package to exist before the publishing API can update it.
3. Create a Google Cloud service account with Google Play Android Developer API access.
4. In Play Console **Users and permissions**, grant that service account access to DroidQuest with
   permission to release to testing tracks. Avoid production-release permission.
5. Create or identify the upload keystore whose certificate is registered in Play App Signing.

## GitHub Actions secrets

Add these repository or protected-environment secrets:

| Secret | Value |
| --- | --- |
| `ANDROID_UPLOAD_KEYSTORE_BASE64` | Base64 of the binary upload `.jks` file (single line) |
| `ANDROID_UPLOAD_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_UPLOAD_KEY_ALIAS` | Upload key alias |
| `ANDROID_UPLOAD_KEY_PASSWORD` | Upload key password |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Full service-account JSON document |

Encode the keystore locally without line wrapping:

```bash
base64 < droidquest-upload.jks | tr -d '\n'
```

The workflow restores the keystore only under the ephemeral runner directory. No credential is
written into the checkout or uploaded as an artifact.

## Versioning and trigger behavior

- Local builds retain the app's checked-in `versionCode` and `versionName`.
- CI uses `100000 + github.run_number` as the Play version code and appends the run number to the
  internal version name. The high offset keeps automated uploads above checked-in development codes.
- A push to `main` is GitHub's reliable post-merge signal. Direct pushes that change the same paths
  also publish; protect `main` and require pull requests if releases must be merge-only.
- The job uses a non-cancelling concurrency group so a newer merge cannot cancel an upload already
  in progress.
