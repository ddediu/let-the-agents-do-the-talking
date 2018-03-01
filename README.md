# Let the agents do the tlking (data and code)

Data and code accompanying Rick Janssen's PhD thesis *Let the agents do the talking*.
More details are available in the thesis itself (not yet published).

This repository is structured by chapters:

  - `chapter4` contains the data and code relevant for **Chapter 4**, which deals with the influence of larynx height on vowel learning and production:
    + `data` contains the speaker model (`JD2.speaker.bz2`) and the results from running the model (`data.csv.bz2`) BZ2-compressed
    + `r_scripts` contains the Rmarkdown script for analyzing the data (`rick-larynx-height.Rmd`)
    + `stat_report` contains the output of the Rmarkdown script (`rick-larynx-height.html`)
    + `training_set` contains details about the training materials (`Training Vowels - Guide.pdf`).

  - `chapter6` contains the data and code relevant for **Chapter 6**, which deals with the influence of hard palate shape on vowel learning and production across generations of iterated language learning:
    + `data` contains the speaker model (`_JD2.speaker.bz2`) and the results from running the model (`_summary.csv.bz2`) BZ2-compressed
    + `r_scripts` contains the Rmarkdown script for analyzing the data (`rick-hard-palate-chains.Rmd`)
    + `stat_report` contains the output of the Rmarkdown script (`rick-hard-palate-chains.html`).
