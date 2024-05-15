## How to release a new version of the project

- Update the version in the `pom.xml` files in a new or existing branch using the following command:

```shell
mvn versions:set -DnewVersion=x.y.z
```

- Commit the changes and push the branch to the repository.
- Create a pull request from the branch to the `main` branch.
- Merge the pull request.
- Create a new release in the repository with the same version as the one set in the `pom.xml` files.
- The artifacts will be available in the GitHub Packages repository after the release is created.
- Update the submodule `bankaxept-epayment-development-kit` and `epp-dev-kit.version` property in a new or existing
   branch in the `bankaxept-epayment-platform` project using the following commands:

```shell
git submodule update --recursive --remote
mvn versions:set-property -Dproperty=epp-dev-kit.version -DnewVersion=x.y.z
```

- Commit the changes and push the branch to the repository.
- Create a pull request from the branch to the `master` branch.
- Merge the pull request.
