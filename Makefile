install-deps:
	mvn validate

test-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealth test

package:
	mvn -DskipTests -DincludeDeps=true package

build-images:
	mvn -Dtest=BenchmarkTest#buildImages surefire:test

test-dockerized-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealthDockerized test


push-images:
	docker push git.project-hobbit.eu:4567/gitadmin/shm-size-system/system-adapter
