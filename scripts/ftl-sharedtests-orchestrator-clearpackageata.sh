gcloud firebase test android run \
  --app=app/build/outputs/apk/debug/app-debug.apk \
  --test=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
  --client-details=matrixLabel="shared tests with orchestrator with clearPackageData" \
  --use-orchestrator \
  --environment-variables clearPackageData=true \
  --device model=Pixel3 \
  --timeout 20m \
  --test-targets "package ca.rmen.android.poetassistant.shared.main"

#  --device model=MediumPhone.arm \
