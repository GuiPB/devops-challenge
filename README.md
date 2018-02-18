# devops-challenge
Outils d'analyse de bucket amazon S3 développé en java

L'outils permet d'obtenir les informations suivantes: 
1) Nom du bucket
2) Date de création
3) Nombre de fichiers
4) Taille totale des fichiers
5) Dernière date de mise-à-jour

L'outils supporte égalemnent les options suivantes:
 - Possibilité de sortir les résultats en octets, Kilooctets, Megaoctets, … ;
 - Pouvoir sortir les informations par type de stockage (Standard, IA, RR) ;
 - Pouvoir spécifier une liste de buckets (bonus si support des expressions régulières) ;
 - Pouvoir regrouper les informations par régions.
 
# Utilisation
Le programme est invoqué à partir de la ligne de commande de la façon suivante:

java -Daws.id -Daws.accessKeyId={monid} -D-Daws.secretKey={secretkey} -jar devop-challenge.jar

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
