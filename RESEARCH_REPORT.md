# OpenSwift Research Report

Research and analysis summary. Detailed historical dependency and v0.2 planning
documents are archived under [docs/archive](docs/archive/).

Last consolidated: 2026-06-01.

## Key Findings

- The app is already past the original v0.2/v0.3 planning docs; the active
  backlog should start at v0.4 multilingual and portability work.
- Dependency analysis identified `material-icons-extended` as the main APK-size
  concern and recommended switching to the smaller Material Icons package where
  possible.
- `androidx.security:security-crypto` was originally called out as alpha-risk;
  the shipped v0.2 encryption work should be periodically rechecked against the
  current AndroidX stable line before the next release.
- The project should keep dependency additions conservative because keyboard
  APK size, startup latency, and privacy expectations matter more than breadth.

## Archived Inputs

- [docs/archive/research/ANALYSIS_INDEX.md](docs/archive/research/ANALYSIS_INDEX.md)
- [docs/archive/research/DEPENDENCY_ANALYSIS.md](docs/archive/research/DEPENDENCY_ANALYSIS.md)
- [docs/archive/research/DEPENDENCY_ANALYSIS_SUMMARY.txt](docs/archive/research/DEPENDENCY_ANALYSIS_SUMMARY.txt)
- [docs/archive/research/DEPENDENCY_TREE.txt](docs/archive/research/DEPENDENCY_TREE.txt)
- [docs/archive/research/OPTIMIZATION_RECOMMENDATIONS.md](docs/archive/research/OPTIMIZATION_RECOMMENDATIONS.md)
- [docs/archive/roadmap/ROADMAP-2026-05-03.md](docs/archive/roadmap/ROADMAP-2026-05-03.md)
- [docs/archive/roadmap/v0.2.0_ROADMAP_INDEX.md](docs/archive/roadmap/v0.2.0_ROADMAP_INDEX.md)
- [docs/archive/roadmap/TASK_CARDS_v0.2.0.md](docs/archive/roadmap/TASK_CARDS_v0.2.0.md)
- [docs/archive/roadmap/DECOMPOSITION_SUMMARY.md](docs/archive/roadmap/DECOMPOSITION_SUMMARY.md)
- [docs/archive/roadmap/QUICK_REF.md](docs/archive/roadmap/QUICK_REF.md)

## Next Research Refresh

Before v0.4 implementation, refresh:

- Current AndroidX Security stable guidance.
- Compose BOM and AGP compatibility.
- Keyboard dictionary package size options for German, French, and Spanish.
- Emoji dataset/package options with license and APK-size impact.
- Privacy implications of optional encrypted sync.
