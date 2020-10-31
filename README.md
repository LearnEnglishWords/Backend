# LEW Backend API:

## Collection endpoints:
**GET** /collection/{id}/  - get collection <br>
**GET** /collection/list/  - get list of collections <br>
**POST** /collection/  - create collection <br>
**PUT** /collection/{id}/  - update collection <br>
**DEL** /collection/{id}/  - delete collection <br>
**GET** /collection/{id}/categories/  - list all categories in collection <br>
**GET** /collection/{id}/words?shuffle={bool}  - list all words in collection <br>


## Category endpoints:
**GET** /category/{id}/  - get category <br>
**GET** /category/list?withWordsCount={bool}  - get list of categories <br>
**POST** /category/  - create category <br>
**PUT** /category/{id}/  - update category <br>
**DEL** /category/{id}/  - delete category <br>

**GET** /category/{id}/words?shuffle={bool}  - list all words in category <br>
**POST** /category/{id}/word/{id}/  - add word into category <br>
**DEL** /category/{id}/word/{id}/  - remove word from category <br>
**POST** /category/{id}/import/  - import words into category <br>
    Content-Type text/plain <br>
    BODY raw

    test
    hello
    celebrate


## Word endpoints:
**GET** /word/{id}/  - get word <br>
**GET** /word/list?page=1&limit=3&state={IMPORT/PARSE/AUTO_PARSE/CORRECT/USER_CORRECT}  - get list of categories <br>
**POST** /word/  - create word <br>
**PUT** /word/{id}/  - update word <br>
**DEL** /word/{id}/  - delete word <br>

**GET** /word/parse?text={word}&filter=true  - parse word from web <br>
**GET** /word/find?text={word}  - find and get word if cannot so parse it from web <br>
**GET** /word/{id}/categories  - get categories where word is added <br>
**GET** /word/updated?from=2020-05-11T00:00:00  - get all updated words from date <br>
**POST** /word/import/  - import words <br> 
    Content-Type text/plain <br>
    BODY raw

    test
    hello
    celebrate


## Activity endpoints:
Activity is for measure of user activities <br>
<br>
**POST** /activity/  - create activity <br>
**GET** /activity?uuid=bbd4fc7957f7e44c  - get activity <br>
**GET** /activity/list  - get list of activities <br>


## Log endpoints:
Log endpoint is for saving of logs from application <br>
<br>
**POST** /log/  - create log <br>
**GET** /log?uuid=bbd4fc7957f7e44c  - get log <br>
**GET** /log/list?from=2020-05-05T11:20:00  - get all logs from date <br>


