### Devops-challenge
Outils d'analyse de bucket amazon S3 développé en java

L'outils permet d'obtenir les informations suivantes: 
- Nom du bucket
- Date de création
- Nombre de fichiers
- Taille totale des fichiers
- Dernière date de mise-à-jour

L'outils supporte égalemnent les options suivantes:
 - Possibilité de sortir les résultats en octets, Kilooctets, Megaoctets, … ;
 - Pouvoir sortir les informations par type de stockage (Standard, IA, RR) ;
 - Pouvoir spécifier une liste de buckets (bonus si support des expressions régulières) ;
 - Pouvoir regrouper les informations par régions.
 
### Utilisation
Le programme est invoqué à partir de la ligne de commande de la façon suivante:

    java -Daws.id -Daws.accessKeyId={monid} -Daws.secretKey={secretkey} -jar devop-challenge.jar [options]

### Options

```

  usage: s3buckettool
 -gr,--group-by-region        Groups results by regions i.e. summarize by
                              region instead of by bucket
 -h,--help                    Prints usage
 -hr,--human-readable         Shows file size in a human readable format.
                              Ex: kB, MB, GB...
 -regex,--regular-exp <arg>   Filter results by bucket name matching a
                              given 'Java Pattern' regular expression
 -st,--stockage-type <arg>    Filters shown information by a specified
                              stockage type
```
### Utilisation sur une instance locale
On peut également lancer le programme sur une instance de [S3Mock](https://github.com/gaul/s3proxy). On peut utiliser l'image docker avec sa configuration de base.

    java -Daws.id -Daws.accessKeyId={monid} -Daws.secretKey={secretkey} -Dspring.profiles.active=integ -jar devop-challenge.jar



### Constuire l'exécutable avec Maven
La commande suivante permet de construire localement l'exécutable.

    mvn clean package

### Exécuter les tests

    mvn clean test

### Exécuter les tests d'intégration
    mvn -Dintegration.test [-Dintegration.port] clean test
Ceci exécute les tests d'intégration. Ceux-ci vont créer une arboresence de fichier dans le dossier courant et simuler un analyse des buckets créés. Le dossier est nettoyé immédiatement après l'exécution

### Rapport de couverture de test
Un rapport de couverture est généré après chaque exécution des tests. Exécutez les tests d'intégation pour un meilleurs résultat de couverture. 