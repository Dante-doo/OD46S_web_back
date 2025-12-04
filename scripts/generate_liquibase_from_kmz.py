#!/usr/bin/env python3
"""
Script para gerar dados do Liquibase a partir dos arquivos KMZ
Inclui rotas de recicláveis e úmidos, polígonos e pontos
"""
import json
import sys
from pathlib import Path

def escape_sql_string(s):
    """Escapa strings para SQL"""
    if s is None:
        return ""
    return s.replace("'", "''")

def geojson_to_sql_string(geojson):
    """Converte GeoJSON para string SQL"""
    return json.dumps(geojson, separators=(',', ':')).replace("'", "''")

def extract_color_from_style(style_id):
    """Extrai cor do style_id do KML"""
    if not style_id:
        return "#0066CC", "#0066CC"
    
    # Tenta extrair cor do style_id (formato: #poly-XXXXXX-1200-77)
    if "poly-" in style_id:
        parts = style_id.split("-")
        if len(parts) >= 2:
            color_hex = parts[1]
            if len(color_hex) == 6:
                # Converte de AABBGGRR para #RRGGBB
                # KML usa formato AABBGGRR, mas precisamos #RRGGBB
                r = color_hex[4:6]
                g = color_hex[2:4]
                b = color_hex[0:2]
                color = f"#{r}{g}{b}"
                return color, color
    
    return "#0066CC", "#0066CC"

def generate_route_inserts(routes_data, waste_type, start_route_id, start_area_id):
    """Gera inserts de rotas e áreas a partir dos dados extraídos"""
    route_id = start_route_id
    area_id = start_area_id
    inserts = []
    
    # Agrupa polígonos e pontos por nome (assumindo que polígonos com mesmo nome pertencem à mesma rota)
    routes_dict = {}
    
    for polygon in routes_data['polygons']:
        name = polygon['name']
        if name not in routes_dict:
            routes_dict[name] = {'polygons': [], 'points': []}
        routes_dict[name]['polygons'].append(polygon)
    
    for point in routes_data['points']:
        name = point['name']
        if name not in routes_dict:
            routes_dict[name] = {'polygons': [], 'points': []}
        routes_dict[name]['points'].append(point)
    
    # Gera inserts
    for route_name, geometries in routes_dict.items():
        # Determina frequência da descrição ou usa padrão
        frequency = "Segunda-feira"  # padrão
        if geometries['polygons']:
            desc = geometries['polygons'][0].get('description', '')
            if desc:
                frequency = desc
        
        # Cria rota
        collection_type = "RESIDENTIAL" if waste_type == "RESIDENTIAL" else "RECYCLABLE"
        periodicity = "0 8 * * 1"  # Segunda-feira às 8h (padrão)
        if "Segunda" in frequency or "segunda" in frequency:
            periodicity = "0 8 * * 1"
        elif "Terça" in frequency or "terça" in frequency:
            periodicity = "0 8 * * 2"
        elif "Quarta" in frequency or "quarta" in frequency:
            periodicity = "0 8 * * 3"
        elif "Quinta" in frequency or "quinta" in frequency:
            periodicity = "0 8 * * 4"
        elif "Sexta" in frequency or "sexta" in frequency:
            periodicity = "0 8 * * 5"
        elif "Todos" in frequency or "todos" in frequency:
            periodicity = "0 8 * * *"
        
        route_insert = f"""        - insert:
            tableName: routes
            columns:
              - column:
                  name: id
                  value: {route_id}
              - column:
                  name: name
                  value: "{escape_sql_string(route_name)}"
              - column:
                  name: description
                  value: "Rota de coleta de resíduos {waste_type.lower()}"
              - column:
                  name: collection_type
                  value: {collection_type}
              - column:
                  name: periodicity
                  value: "{periodicity}"
              - column:
                  name: priority
                  value: "MEDIUM"
              - column:
                  name: active
                  valueBoolean: true
              - column:
                  name: created_by
                  value: 1"""
        inserts.append(route_insert)
        
        # Adiciona polígonos como route_areas
        for polygon in geometries['polygons']:
            stroke_color, fill_color = extract_color_from_style(polygon.get('style_id'))
            geojson_str = geojson_to_sql_string(polygon['geometry'])
            
            area_insert = f"""        - sql:
            sql: |
              INSERT INTO route_areas (id, route_id, external_name, waste_type, geometry_geojson, stroke_color, fill_color, fill_opacity, active)
              VALUES ({area_id}, {route_id}, '{escape_sql_string(polygon['name'])}', '{waste_type}', '{geojson_str}'::jsonb, '{stroke_color}', '{fill_color}', 0.40, true);"""
            inserts.append(area_insert)
            area_id += 1
        
        # Adiciona pontos como route_areas (tipo Point)
        for point in geometries['points']:
            stroke_color, fill_color = extract_color_from_style(point.get('style_id'))
            geojson_str = geojson_to_sql_string(point['geometry'])
            
            area_insert = f"""        - sql:
            sql: |
              INSERT INTO route_areas (id, route_id, external_name, waste_type, geometry_geojson, stroke_color, fill_color, fill_opacity, active)
              VALUES ({area_id}, {route_id}, '{escape_sql_string(point['name'])}', '{waste_type}', '{geojson_str}'::jsonb, '{stroke_color}', '{fill_color}', 0.40, true);"""
            inserts.append(area_insert)
            area_id += 1
        
        route_id += 1
    
    return inserts, route_id, area_id

def main():
    base_dir = Path(__file__).parent.parent
    docs_dir = base_dir / "docs"
    
    # Carrega dados extraídos
    with open(docs_dir / "kmz_extracted_data.json", 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    reciclavel_data = data['reciclavel']
    umidos_data = data['umidos']
    
    print(f"Processando dados:")
    print(f"  Recicláveis: {len(reciclavel_data['polygons'])} polígonos, {len(reciclavel_data['points'])} pontos")
    print(f"  Úmidos: {len(umidos_data['polygons'])} polígonos, {len(umidos_data['points'])} pontos")
    
    # Verifica quantas rotas já existem (assumindo que começam do ID 1)
    # Vou assumir que já existem 41 rotas de recicláveis (baseado no insert-initial-data)
    # Mas vou verificar se precisamos adicionar mais ou criar novas
    
    # Gera inserts para rotas úmidas (começando do ID 42, assumindo 41 rotas recicláveis)
    start_route_id = 42
    start_area_id = 42  # Assumindo 41 áreas já existentes
    
    umidos_inserts, next_route_id, next_area_id = generate_route_inserts(
        umidos_data, "RESIDENTIAL", start_route_id, start_area_id
    )
    
    # Corrige collection_type nos inserts
    umidos_inserts = [insert.replace('value: RECYCLABLE', 'value: RESIDENTIAL') if 'collection_type' in insert else insert for insert in umidos_inserts]
    
    print(f"\nGerados {len(umidos_inserts)} inserts para rotas úmidas")
    print(f"Próximo route_id: {next_route_id}, próximo area_id: {next_area_id}")
    
    # Salva em arquivo YAML
    output_file = docs_dir / "liquibase_umidos_inserts.yml"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("  # ==========================================\n")
        f.write("  # CHANGESET: Insert routes and areas for UMIDOS (Residential waste)\n")
        f.write("  # ==========================================\n")
        f.write("  - changeSet:\n")
        f.write("      id: 019-insert-umidos-routes-and-areas\n")
        f.write("      author: od46s-team\n")
        f.write("      changes:\n")
        for insert in umidos_inserts:
            f.write(insert + "\n")
        f.write("\n")
        f.write("  # ==========================================\n")
        f.write("  # CHANGESET: Reset sequences after umidos inserts\n")
        f.write("  # ==========================================\n")
        f.write("  - changeSet:\n")
        f.write("      id: 020-reset-sequences-after-umidos\n")
        f.write("      author: od46s-team\n")
        f.write("      changes:\n")
        f.write(f"        - sql:\n")
        f.write(f"            sql: \"SELECT setval('routes_id_seq', {next_route_id - 1}, true);\"\n")
        f.write(f"            dbms: postgresql\n")
        f.write(f"        - sql:\n")
        f.write(f"            sql: \"SELECT setval('route_areas_id_seq', {next_area_id - 1}, true);\"\n")
        f.write(f"            dbms: postgresql\n")
    
    print(f"\nArquivo gerado: {output_file}")
    print(f"\nPara usar, copie o conteúdo do arquivo e adicione ao final de 003-insert-initial-data.yml")

if __name__ == "__main__":
    main()

