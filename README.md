# generate-map-cicd
Building image from map ; CI/CD step.

## Usage
```yml
name: Map image generator test
run-name: ${{ github.actor }} started map image generation 🚀
on: [push, workflow_dispatch]
jobs:
  Explore-GitHub-Actions:
    runs-on: ubuntu-latest
    steps:
      - name: The cyclist diary - image map generator
        uses: the-cyclist-diary/generate-map-cicd@main
        with:
          content-path: "./"
          user-token: ${{ secrets.GITHUB_TOKEN }}
```
