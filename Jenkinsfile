node {
  checkout([
    $class: 'GitSCM',
    branches: [[name: 'master']],
    doGenerateSubmoduleConfigurations: false,
    extensions: [],
    submoduleCfg: [],
    userRemoteConfigs: [[url: 'https://github.com/caarmen/poet-assistant']]])

  withCredentials([
    file(credentialsId: 'storeFile', variable: 'AndroidSigningStoreFile'),
    string(credentialsId: 'keyAlias', variable: 'AndroidSigningKeyAlias'),
    string(credentialsId: 'keyPassword', variable: 'AndroidSigningKeyPassword'),
    string(credentialsId: 'storePassword', variable: 'AndroidSigningStorePassword')]) {

    sh "./gradlew clean assembleRelease"
  }

  archiveArtifacts 'app/build/outputs/apk/*.apk'
}
