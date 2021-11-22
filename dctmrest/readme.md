Custom resource for consuming registered tables on Documentum

# Endpoint
/repositories/{repositoryName}/registeredtable/{table_name}/row

# Example body messages
## POST
{"rows" : 
    [
        {"row" : {
            "test_id" : 4,
            "test_char" : "test4"
            }
        }
    ]
}

## PUT
{
    "predicate" : [
        {
        "set" : "test_id=2",
        "where" : "test_id=8"
        }
    ]
}

## DELETE
{
    "predicate" : [
        {
        "where" : "test_bool='true'"
        }
    ]
}