import json
import boto3
s3 = boto3.client('s3')

def read_data (source_bucket, source_key, data):
    try:
        response = s3.get_object(Bucket=source_bucket, Key=source_key)
        content = response['Body'].read().decode('utf-8')
        data = json.loads(content)
    except Exception as e:
        print(f"❌ Error leyendo el archivo original: {e}")
        return e
    
def save_data (target_bucket, target_key):
    print(f"📂 Guardando archivo procesado en: {target_bucket}/{target_key}")

    try:
        s3.put_object(
            Bucket=target_bucket,
            Key=target_key,
            Body=json.dumps(data, indent=2, ensure_ascii=False),
            ContentType='application/json'
        )
        print("✅ Archivo modificado y guardado correctamente")
    except Exception as e:
        print(f"❌ Error al guardar el archivo modificado: {e}")