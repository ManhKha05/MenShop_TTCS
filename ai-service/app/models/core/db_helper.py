import pandas as pd
from sqlalchemy import create_engine, text

class DatabaseHelper:
    def __init__(self):
        self.user = "root"
        self.password = "322005"
        self.host = "localhost"
        self.port = "3306"
        self.db_name = "menshop"

        self.connection_str = f"mysql+pymysql://{self.user}:{self.password}@{self.host}:{self.port}/{self.db_name}"
        self.engine = create_engine(
            self.connection_str,
            pool_size=10,
            max_overflow=20,
            pool_recycle=3600
        )

    def fetch_products_for_ai(self):
        query = """
            SELECT id, name, description, attributes_json, category_id 
            FROM product 
            WHERE status = 'ACTIVE'
        """
        return pd.read_sql(query, self.engine)

    def fetch_interactions_for_cf(self):
        query = """
            SELECT 
                user_id, 
                product_id, 
                SUM(weight_score) as weight,
                MAX(created_at) as created_at
            FROM user_interaction
            WHERE user_id IN (
                SELECT user_id 
                FROM user_interaction 
                GROUP BY user_id 
                HAVING COUNT(id) > 1 OR SUM(weight_score) > 1.0
            )
            GROUP BY user_id, product_id
        """
        return pd.read_sql(query, self.engine)

    def get_product_details(self, product_ids):
        if not product_ids:
            return pd.DataFrame(columns=['id', 'name'])

        query = text("SELECT id, name FROM product WHERE id IN :ids")
        return pd.read_sql(query, self.engine, params={"ids": tuple(product_ids)})