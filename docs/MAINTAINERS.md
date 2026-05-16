MAINTAINERS INFO
==========================================

## Contributing

For general PR creation and workflow, see [the contributing document][Contributing].

## Porting

For coordinating and porting NeoForge to new Minecraft versions, see [the porting workflow][Porting].
Non-maintainers are not able to run all the required tooling for this.

## Porting Labels

PRs can be backported automatically to a specific minecraft version by having maintainers add the `backport to x.xx.x` label to the PR.
A bot will generate a new PR and handle backporting to the best of its abilities but the PR may need manual fixing if it runs into issues.
It is a good idea to test the backport PRs before merging in case the PR functionality is impacted by the older codebase.

## Releases

If a PR needs to be merged without triggering the workflow to generate a NeoForge release, follow [the documentation here](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/skip-workflow-runs) for how to skip workflow runs. Essentially adding `[skip ci]` to the PR merge commit message will suffice.

## Notes

- PRs that had requested changes from a maintainer or has another maintainer assigned to the PR, these maintainer(s) should be contacted first before merging the PR to ensure they are ok with the final form of the PR and that their concerns were properly addressed. Along these lines, a maintainer's review should not be "dismissed" without checking with the maintainer.
- Breaking change window for the NeoForge project will generally aim to last about 1 month after a significant Minecraft version.
    - Hotfix Minecraft versions or very small Minecraft versions will not reset the breaking change window.
    - This window is flexible and may be longer if there is significant breaking change PRs that need to be released for that Minecraft version but not yet ready. The 1 month timeframe is just a goal we would like to achieve, but we understand it is not always possible or optimal.

[Contributing]: ../docs/CONTRIBUTING.md
[Porting]: ../docs/PORTING.md
