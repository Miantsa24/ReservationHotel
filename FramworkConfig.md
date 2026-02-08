Solutions
Option 1 : Installer Git (Recommandé)
Téléchargez Git depuis : https://git-scm.com/download/win
Installez-le avec les options par défaut
Redémarrez VS Code après l'installation

Parfait ! Installons Git sur votre système Windows.
winget install --id Git.Git -e --source winget

Pour que Git soit accessible dans le terminal, il faut rafraîchir le PATH. Laissez-moi ouvrir un nouveau terminal et ajouter votre framework :

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User"); cd d:\Hotel; git submodule add https://github.com/Miantsa24/SPRINT1.git framework  (ovaina Pseudo sy anaranle repository anle framework)