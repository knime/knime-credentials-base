#!groovy
def BN = (BRANCH_NAME == 'master' || BRANCH_NAME.startsWith('releases/')) ? BRANCH_NAME : 'releases/2025-12'

library "knime-pipeline@$BN"

properties([
    pipelineTriggers([
        // knime-tp -> knime-base -> knime-svg -> knime-js-core -> knime-workbench
        upstream('knime-workbench/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
        // knime-core -> knime-core-ui -> knime-gateway
        upstream('knime-gateway/' + env.BRANCH_NAME.replaceAll('/', '%2F'))
    ]),
    parameters(workflowTests.getConfigurationsAsParameters()),
    buildDiscarder(logRotator(numToKeepStr: '5')),
    disableConcurrentBuilds()
])


try {
    knimetools.defaultTychoBuild('org.knime.update.credentials.base')
    
    workflowTests.runTests(
        dependencies: [
            repositories: [
                'knime-gateway',
                'knime-xml',
                'knime-json',
                'knime-kerberos',
                'knime-rest',
                'knime-js-base',
                'knime-js-core'
            ]
        ]
    )

    stage('Sonarqube analysis') {
        env.lastStage = env.STAGE_NAME
        workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result);
}
/* vim: set shiftwidth=4 expandtab smarttab: */
