### Objetivo do projeto

Vamos montar um projeto de um micro-serviço que vai  ter as seguintes funções:

1. Receber imagens enviadas via json, registrar as informações da imagem, enviar as imagens para ser armazenadas para o servidor de armazenamento da Cloudflare Images. Uma vez que enviou a imagem para o banco de dados da Cloudflare deve registrar os dados de id das URLs que serão devolvidas pela da API Cloudflare.
2. Disponibilizar os endpoints de CRUD e de consultas das URLs.
3. Para segurança vamos precisar implementar um controle de acesso por token JWT que pode ter a duração de 30 minutos. No código contexto do token vai retornar o código do empresa para que nas outras requisições não seja necessário informar a identificação da empresa somente o token.
4. Variáveis de ambiente:
getTokenCloudflare  uiXHR6nRCvOmsEuJ7Rm0Pv530NS7A_QaYr38KxbN
postTokenCloudflare zXnfAoOiLUHdFJhLxjhJW7c9nGxlxX3BduO2IX2Y

Tabelas do projeto

tb_usuario

id long
idEmpresa long
nome string (30)
senha string(20)
role enum ADMINISTRADOR - USUARIO

tb_empresa

id long
codigoErp string (20)


tb_produto 

id string
descricao string (80)
discriminacao string (200)
codigoErp string(20)
idEmpresa long (não obrigatório)
codigoEan string(13)

tb_imagem (cada produto vai ter varios registro tb_imagem)

id string
idProduto 
idEmpresa long (não obrigatório)
tipoArmazenamento integer 0 - aberto 1 - de uso somente da empresa idEmpresa
idImagem string (50) - vai registrar o id da imagem retornado pela Cloudflare
url text

Vamos precisar dos seguintes endpoints

1. Endpoint de CRUD para usuário, empresa, produto e imagem
2. o CRUD de Create e Update da imagem vai precisar receber uma lista dos arquivos das imagens que não vai ficar armazenado somente para enviar a imagem para a Cloudflare
3. Consulta de imagens por id da imagem
4. Consulta de imagens por codigoEan. Vai retornar todos os produtos com o mesmo CodigoErp e que seja o tipoArmazenamento = 0 (aberto)
5. O CRUD de Create e Update do produto vai precisa receber um json com os dados do produto e uma lista de imagens

Para acessar os endpoinst da Cloudflare
1. Para verificar validade do token 
Get: https://api.cloudflare.com/client/v4/user/tokens/verify
Autorization: Auth Type o conteúdo vai ser a variável de ambiente getTokenCloudflare ou postTokenCloudflare
Exemplo de json de retorno: 
{
    "result": {
        "id": "b6b02737e9a0792471c22e1f33315327",
        "status": "active"
    },
    "success": true,
    "errors": [],
    "messages": [
        {
            "code": 10000,
            "message": "This API Token is valid and active",
            "type": null
        }
    ]
}
2. Para enviar imagem
    https://api.cloudflare.com/client/v4/accounts/8e858ade7b52abbd9f51c3071f0cbf42/images/v1

    headers: Authorization postTokenCloudflare
    body: file a imagem recebida via endpoint como arquivo

    retorno do status 200 
    {
    "result": {
        "id": "1b0431d6-6033-45fe-bd62-a51ca3707400",
        "filename": "7896286614856.png",
        "uploaded": "2026-02-18T23:02:14.153Z",
        "requireSignedURLs": false,
        "variants": [
            "https://imagedelivery.net/y5-ViFFoppEAumgtf-MYLQ/1b0431d6-6033-45fe-bd62-a51ca3707400/public"
        ]
    },
    "success": true,
    "errors": [],
    "messages": []
}
Vai ser precisa armazenar os dados do id da imagem e da URL que retornar em variants














