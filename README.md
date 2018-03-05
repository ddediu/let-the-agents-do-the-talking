# Let the agents do the talking (data and code)

Data and code accompanying **Rick Janssen's** PhD thesis *Let the agents do the talking*.
More details are available in the thesis itself (not yet published).

Most of the code and data are (c) Rick Janseen, with contributions from Scott R. Moisik and Dan Dediu.

This repository is structured by chapters:

  - `chapter4` contains the data and code relevant for **Chapter 4**, which deals with the influence of larynx height on vowel learning and production:
    + `data` contains the speaker model (`JD2.speaker.bz2`) and the results from running the model (`data.csv.bz2`) BZ2-compressed
    + `r_scripts` contains the Rmarkdown script for analyzing the data (`rick-larynx-height.Rmd`)
    + `stat_report` contains the bz2-compressed output of the Rmarkdown script (`rick-larynx-height.html.bz2`)
    + `training_set` contains details about the training materials (`Training Vowels - Guide.pdf`).

  - `chapter6` contains the data and code relevant for **Chapter 6**, which deals with the influence of hard palate shape on vowel learning and production across generations of iterated language learning:
    + `data` contains the speaker model (`_JD2.speaker.bz2`) and the results from running the model (`_summary.csv.bz2`) BZ2-compressed
    + `r_scripts` contains the Rmarkdown script for analyzing the data (`rick-hard-palate-chains.Rmd`)
    + `stat_report` contains the output of the Rmarkdown script bz2-compressed and split into volumnes of less than 20Mb (`rick-hard-palate-chains.html`).
    
  - `appendixA` contains the source code and compiled binaries for all the contributions described in this thesis, namely:
    + `binaries` contains the compiled binaries for the:
      + `agent`: the agent
      + `standalone`: the modified VTL including the Bézier hard palate model
    + `source`: the (unless specified, GPLv3-governed) source code for:
      + `Agent`: the full agent code (`Java` and `Python`)
      + `bezier`: the code for the Bézier hard palate shape (`Python`)
      + `NativeLib`: the interface to VTL (`Java`)
      + `VTL`: placeholder for the modified VTL2.1 code (`C++`).


Unless otherwise specified, the license governing these is [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt).
An important exception is the source code of VTL2.1 and the modifications we brought to it, which are governed by a custom license, derived from the VTL2.1 source code license as described in the `AppendixA/source/License-VTL-modified-2018.pdf` document, license that makes is free distribution not possible (but it may be distributed by request after agreeing to the terms of this license).

