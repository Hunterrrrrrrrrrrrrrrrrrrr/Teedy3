pipeline {
agent any
stages {
stage('Clean') {
steps {
sh 'mvn clean'
}
}
stage('Compile') {
steps {
sh 'mvn compile -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
stage('Test') {
steps {
sh 'mvn test -Dmaven.test.failure.ignore=true -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
stage('PMD') {
steps {
sh 'mvn pmd:pmd -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
stage('JaCoCo') {
steps {
sh 'mvn jacoco:report -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
stage('Javadoc') {
steps {
sh 'mvn javadoc:javadoc -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
stage('Site') {
steps {
sh 'mvn site -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
stage('Package') {
steps {
sh 'mvn package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17'
}
}
}
post {
always {
archiveArtifacts artifacts: '**/target/site/**/*.*', fingerprint: true
archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
archiveArtifacts artifacts: '**/target/**/*.war', fingerprint: true
junit '**/target/surefire-reports/*.xml'
}
}
}
