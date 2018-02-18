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

### Performance
Une attention particulière a été mise sur la performance. En effet, AmazonS3 ne fait pas la distinction entre un fichier ou un dossier. En effet, tout chemin de fichier fait partie du nom de fichier (key). Pour simuler une arborescence, on peut exploiter les prefix communs retournés par la fonction listObjects(). Le programme va découper les listes d'objet en prefix pour permettre à d'autre processus d'effectuer des requêtes à l'aide des préfix.

Le traitement a été testé selon les hypopthèse suivantes:

- Le délimiteur de dossier sera le caractère '/'
- Que tout appel à listObjects() retourne toujours tous les prefix possibles

En effet, pour ce dernier point, au moment d'écrire ces lignes, la documentation de Amazon ne semble pas évoquer qu'il faille effectuer de multiples appels pour obtenir tous les préfix. Contrairement au fichiers, qui seront retournés par lot de 1000 entrées.

 
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
Ceci exécute les tests d'intégration à l'aide d'une instance de [S3Mock](https://github.com/gaul/s3proxy). On peut choisir le port de communication à l'aide de la proriété de JVM optionnelle. Les tests vont créer une arboresence de fichier dans le dossier courant et simuler un analyse des buckets créés. Le dossier est nettoyé immédiatement après l'exécution.

### Rapport de couverture de test
Un rapport de couverture est généré après chaque exécution des tests. Exécutez les tests d'intégation pour un meilleurs résultat de couverture. 