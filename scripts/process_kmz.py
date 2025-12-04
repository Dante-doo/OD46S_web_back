#!/usr/bin/env python3
"""
Script para processar arquivos KMZ e extrair dados de rotas (polígonos e pontos)
"""
import zipfile
import xml.etree.ElementTree as ET
import json
import sys
from pathlib import Path

# Namespace do KML
KML_NS = {'kml': 'http://www.opengis.net/kml/2.2'}

def parse_coordinates(coord_string):
    """Converte string de coordenadas KML para lista de [lon, lat]"""
    coords = []
    for coord in coord_string.strip().split():
        parts = coord.split(',')
        if len(parts) >= 2:
            lon = float(parts[0])
            lat = float(parts[1])
            coords.append([lon, lat])
    return coords

def kml_to_geojson_geometry(placemark):
    """Converte um Placemark KML para geometria GeoJSON"""
    geometry = None
    geom_type = None
    
    # Verifica se é um Polygon
    polygon = placemark.find('.//kml:Polygon', KML_NS)
    if polygon is not None:
        outer_boundary = polygon.find('.//kml:outerBoundaryIs/kml:LinearRing/kml:coordinates', KML_NS)
        if outer_boundary is not None:
            coords = parse_coordinates(outer_boundary.text)
            if len(coords) > 0:
                # GeoJSON Polygon precisa fechar o anel (primeiro ponto = último ponto)
                if coords[0] != coords[-1]:
                    coords.append(coords[0])
                geometry = {
                    "type": "Polygon",
                    "coordinates": [coords]
                }
                geom_type = "Polygon"
    
    # Verifica se é um Point
    point = placemark.find('.//kml:Point', KML_NS)
    if point is not None:
        coords_elem = point.find('.//kml:coordinates', KML_NS)
        if coords_elem is not None:
            coords = parse_coordinates(coords_elem.text)
            if len(coords) > 0:
                geometry = {
                    "type": "Point",
                    "coordinates": coords[0]
                }
                geom_type = "Point"
    
    return geometry, geom_type

def extract_kmz_data(kmz_path):
    """Extrai dados de um arquivo KMZ"""
    data = {
        'polygons': [],
        'points': []
    }
    
    with zipfile.ZipFile(kmz_path, 'r') as kmz:
        # Procura pelo arquivo KML
        kml_files = [f for f in kmz.namelist() if f.endswith('.kml')]
        if not kml_files:
            print(f"Erro: Nenhum arquivo KML encontrado em {kmz_path}")
            return data
        
        kml_content = kmz.read(kml_files[0])
        root = ET.fromstring(kml_content)
        
        # Encontra todos os Placemarks
        placemarks = root.findall('.//kml:Placemark', KML_NS)
        
        for placemark in placemarks:
            name_elem = placemark.find('kml:name', KML_NS)
            name = name_elem.text if name_elem is not None else "Sem nome"
            
            description_elem = placemark.find('kml:description', KML_NS)
            description = description_elem.text if description_elem is not None else ""
            
            # Extrai estilo se existir
            style_url = placemark.find('kml:styleUrl', KML_NS)
            style_id = style_url.text if style_url is not None else None
            
            geometry, geom_type = kml_to_geojson_geometry(placemark)
            
            if geometry:
                feature = {
                    "name": name,
                    "description": description,
                    "style_id": style_id,
                    "geometry": geometry,
                    "type": geom_type
                }
                
                if geom_type == "Polygon":
                    data['polygons'].append(feature)
                elif geom_type == "Point":
                    data['points'].append(feature)
    
    return data

def main():
    base_dir = Path(__file__).parent.parent
    docs_dir = base_dir / "docs"
    
    # Processa arquivo de recicláveis
    reciclavel_kmz = docs_dir / "map_reciclavel.kmz"
    umidos_kmz = docs_dir / "map_umidos.kmz"
    
    print("Processando map_reciclavel.kmz...")
    reciclavel_data = extract_kmz_data(reciclavel_kmz)
    print(f"  - Polígonos encontrados: {len(reciclavel_data['polygons'])}")
    print(f"  - Pontos encontrados: {len(reciclavel_data['points'])}")
    
    print("\nProcessando map_umidos.kmz...")
    umidos_data = extract_kmz_data(umidos_kmz)
    print(f"  - Polígonos encontrados: {len(umidos_data['polygons'])}")
    print(f"  - Pontos encontrados: {len(umidos_data['points'])}")
    
    # Salva resultados em JSON para análise
    output_file = docs_dir / "kmz_extracted_data.json"
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump({
            'reciclavel': reciclavel_data,
            'umidos': umidos_data
        }, f, indent=2, ensure_ascii=False)
    
    print(f"\nDados extraídos salvos em: {output_file}")
    
    # Mostra alguns exemplos
    if reciclavel_data['polygons']:
        print("\nExemplo de polígono reciclável:")
        print(f"  Nome: {reciclavel_data['polygons'][0]['name']}")
        print(f"  Tipo: {reciclavel_data['polygons'][0]['type']}")
    
    if reciclavel_data['points']:
        print("\nExemplo de ponto reciclável:")
        print(f"  Nome: {reciclavel_data['points'][0]['name']}")
        print(f"  Tipo: {reciclavel_data['points'][0]['type']}")
    
    if umidos_data['polygons']:
        print("\nExemplo de polígono úmidos:")
        print(f"  Nome: {umidos_data['polygons'][0]['name']}")
        print(f"  Tipo: {umidos_data['polygons'][0]['type']}")
    
    if umidos_data['points']:
        print("\nExemplo de ponto úmidos:")
        print(f"  Nome: {umidos_data['points'][0]['name']}")
        print(f"  Tipo: {umidos_data['points'][0]['type']}")

if __name__ == "__main__":
    main()

