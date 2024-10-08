# generate-map-cicd
Building image from map ; CI/CD step.

## Usage
```yml
name: IMage from GPX generator
run-name: ${{ github.actor }} started map image generation 🚀
on: [push]
jobs:
  Generate images from GPX files:
    runs-on: ubuntu-latest
    steps:
      - name: Generate images
        uses: the-cyclist-diary/generate-map-cicd@v1.2.1
        with:
          content-path: "./"
          user-token: ${{ secrets.GITHUB_TOKEN }}
```
