/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.visualizer.school.domain.model.school.assembler;

import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.library.swing.visualizer.school.domain.model.school.School;
import org.qi4j.library.swing.visualizer.school.domain.model.school.SchoolId;
import org.qi4j.library.swing.visualizer.school.domain.model.school.SchoolRepository;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.service.ServiceComposite;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( SchoolRepositoryService.SchoolRepositoryMixin.class )
interface SchoolRepositoryService extends SchoolRepository, ServiceComposite
{
    class SchoolRepositoryMixin
        implements SchoolRepository
    {
        @Structure private UnitOfWorkFactory uowf;

        public final Query<School> findAll()
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();

            try
            {
                QueryBuilderFactory qbf = uow.queryBuilderFactory();
                return qbf.newQueryBuilder( School.class ).newQuery();
            }
            finally
            {
                uow.pause();
            }
        }

        public final School find( SchoolId schoolId )
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();
            try
            {
                School school = uow.find( schoolId.idString(), School.class );
                uow.completeAndContinue();
                return school;
            }
            catch( EntityCompositeNotFoundException e )
            {
                uow.discard();
                return null;
            }
            catch( UnitOfWorkCompletionException e )
            {
                // Shouldn't happened
                return null;
            }
        }
    }
}