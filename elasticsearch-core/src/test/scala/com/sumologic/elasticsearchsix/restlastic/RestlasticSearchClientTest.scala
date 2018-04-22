/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package com.sumologic.elasticsearchsix.restlastic

import com.sumologic.elasticsearchsix.restlastic.RestlasticSearchClient.ReturnTypes.{ElasticJsonDocument, ElasticErrorResponse, IndexAlreadyExistsException, BucketAggregationResultBody, Bucket, BucketNested}
import com.sumologic.elasticsearchsix.restlastic.dsl.Dsl._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import spray.http.HttpMethods._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class RestlasticSearchClientTest extends WordSpec with Matchers with BeforeAndAfterAll
    with ElasticsearchIntegrationTest with OneInstancePerTest {
  val tpe = Type("foo")
  val analyzerName = Name("keyword_lowercase")

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val patience = PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(50, Millis)))

  val analyzer = Analyzer(analyzerName, Keyword, Lowercase)
  val indexSetting = IndexSetting(12, 1, analyzer, 30)
  val indexFut = restClient.createIndex(index, Some(indexSetting))
  indexFut.futureValue

  private def refreshWithClient(): Unit = {
    Await.result(restClient.refresh(index), 2.seconds)
  }

  "RestlasticSearchClient" should {
    val basicTextFieldMapping = BasicFieldMapping(TextType, None, Some(analyzerName), ignoreAbove = Some(10000), Some(analyzerName))
    val basicKeywordFieldMapping = BasicFieldMapping(KeywordType, None, None, ignoreAbove = None, None)
    val basicNumericFieldMapping = BasicFieldMapping(IntegerType, None, None, None, None)

    def indexDocs(docs: Seq[Document]): Unit = {
      val bulkIndexFuture = restClient.bulkIndex(index, tpe, docs)
      whenReady(bulkIndexFuture) { _ => refresh() }
    }
  }
}

case class DocNestedType(user: List[Map[String, String]])

case class DocType(f1: String, f2: Int)

