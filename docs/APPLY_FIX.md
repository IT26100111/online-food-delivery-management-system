# Apply the integration fix

This package was prepared from main commit b868e13. It has not been pushed to GitHub.

1. Save your current work first. In IntelliJ Terminal run `git status`. Commit any intended local work on your feature branch before switching branches.
2. With a clean working tree, update main and create a fix branch:

```powershell
git switch main
git pull --ff-only origin main
git switch -c fix/delivery-routing
```

3. Extract this ZIP outside your project. It contains `foodflow-integration-fix.patch` and a `replacement-files` folder. Prefer the patch: from your project root run the following, replacing the example path with the actual extraction location:

```powershell
git apply --check "C:\Users\YOUR_NAME\Downloads\foodflow-integration-fix\foodflow-integration-fix.patch"
git apply "C:\Users\YOUR_NAME\Downloads\foodflow-integration-fix\foodflow-integration-fix.patch"
```

If the check reports a mismatch, stop and compare the affected files; do not force it. The ZIP also contains the changed/new files under their original relative paths for manual comparison. Preserve any local database credentials when comparing application.properties.

4. Set IntelliJ Project SDK and Maven runner JRE to Java 21. Reload Maven after applying the changes, then run:

```powershell
.\mvnw.cmd test
```

5. Start the app and perform the manual Delivery checks described in INTEGRATION_AUDIT.md. Review `git diff`, commit the changes, and push:

```powershell
git add .
git commit -m "Fix delivery forms and module navigation"
git push -u origin fix/delivery-routing
```

Create a pull request with base `main` and compare `fix/delivery-routing`. Merge after the tests and manual checks pass.
